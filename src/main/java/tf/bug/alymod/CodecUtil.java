package tf.bug.alymod;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.Nullable;

public class CodecUtil {

    public static <T, E, U> MapCodec<T> extend(
            final MapCodec<T> original,
            final Function<T, @Nullable E> getData,
            final String fieldName,
            final Function<T, T> cloner,
            final Function<E, Codec<U>> field,
            final Function<T, U> accessor,
            final BiConsumer<T, U> setter
    ) {
        return original.mapResult(new MapCodec.ResultFunction<>() {
            @Override
            public <X> DataResult<T> apply(DynamicOps<X> ops, MapLike<X> input, DataResult<T> a) {
                return a.flatMap(t -> {
                    E doExtension = getData.apply(t);
                    if(doExtension != null) {
                        X encodedValue = input.get(fieldName);
                        DataResult<Pair<U, X>> value = field.apply(doExtension).decode(ops, encodedValue);
                        return value.map(u -> {
                            T copy = cloner.apply(t);
                            setter.accept(copy, u.getFirst());
                            return copy;
                        });
                    } else return DataResult.success(t);
                });
            }

            @Override
            public <X> RecordBuilder<X> coApply(DynamicOps<X> ops, T input, RecordBuilder<X> t) {
                E doExtension = getData.apply(input);
                if(doExtension != null) {
                    U value = accessor.apply(input);
                    return t.add(fieldName, value, field.apply(doExtension));
                } else return t;
            }
        });
    }

    public static <X, T, E, U> PacketCodec<X, T> extendPacket(
            final PacketCodec<? super X, T> original,
            final Function<T, @Nullable E> getData,
            final Function<T, T> cloner,
            final Function<E, PacketCodec<? super X, U>> field,
            final Function<T, U> accessor,
            final BiConsumer<T, U> setter
    ) {
        return new PacketCodec<>() {
            @Override
            public T decode(X buf) {
                T originalT = original.decode(buf);
                E doExtension = getData.apply(originalT);
                if(doExtension != null) {
                    T newT = cloner.apply(originalT);
                    U u = field.apply(doExtension).decode(buf);
                    setter.accept(newT, u);
                    return newT;
                } else return originalT;
            }

            @Override
            public void encode(X buf, T value) {
                original.encode(buf, value);
                E doExtension = getData.apply(value);
                if(doExtension != null) {
                    field.apply(doExtension).encode(buf, accessor.apply(value));
                }
            }
        };
    }

}
