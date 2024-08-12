package tf.bug.alymod;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.network.codec.PacketCodec;

public class CodecUtil {

    public static <T, U> MapCodec<T> extend(
            final MapCodec<T> original,
            final Predicate<T> when,
            final String fieldName,
            final Function<T, T> cloner,
            final Codec<U> field,
            final Function<T, U> accessor,
            final BiConsumer<T, U> setter
    ) {
        return original.mapResult(new MapCodec.ResultFunction<>() {
            @Override
            public <X> DataResult<T> apply(DynamicOps<X> ops, MapLike<X> input, DataResult<T> a) {
                return a.flatMap(t -> {
                    if(when.test(t)) {
                        X encodedValue = input.get(fieldName);
                        DataResult<Pair<U, X>> value = field.decode(ops, encodedValue);
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
                if(when.test(input)) {
                    U value = accessor.apply(input);
                    return t.add(fieldName, value, field);
                } else return t;
            }
        });
    }

    public static <X, T, U> PacketCodec<X, T> extendPacket(
            final PacketCodec<? super X, T> original,
            final Predicate<T> when,
            final Function<T, T> cloner,
            final PacketCodec<? super X, U> field,
            final Function<T, U> accessor,
            final BiConsumer<T, U> setter
    ) {
        return new PacketCodec<>() {
            @Override
            public T decode(X buf) {
                T originalT = original.decode(buf);
                if(when.test(originalT)) {
                    T newT = cloner.apply(originalT);
                    U u = field.decode(buf);
                    setter.accept(newT, u);
                    return newT;
                } else return originalT;
            }

            @Override
            public void encode(X buf, T value) {
                original.encode(buf, value);
                if(when.test(value)) {
                    field.encode(buf, accessor.apply(value));
                }
            }
        };
    }

}
