package tf.bug.alymod;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.lang.invoke.MethodHandle;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4i;

// https://www.geometrictools.com/Documentation/IntersectionBoxCone.pdf
public final class ConeBoxIntersection {
    private ConeBoxIntersection() {}

    private static final record Face(
            int v0,
            int v1,
            int v2,
            int v3,
            int e0,
            int e1,
            int e2,
            int e3
    ) {}

    public static double[] computeBoxHeightInterval(Box box, Vec3d coneAxisD) {
        Vec3d centerC = box.getCenter();
        Vec3d offsetE = box.getMaxPos().subtract(box.getMinPos()).multiply(0.5);
        double distanceDdCmV = coneAxisD.dotProduct(centerC);
        double radius = (offsetE.x * Math.abs(coneAxisD.x)) +
                (offsetE.y * Math.abs(coneAxisD.y)) +
                (offsetE.z * Math.abs(coneAxisD.z));
        return new double[] {
                distanceDdCmV - radius,
                distanceDdCmV + radius
        };
    }

    public static double lineSegmentIntersectsBox(Box box, Vec3d ray, double length) {
        double dfx = 1.0d / ray.x;
        double dfy = 1.0d / ray.y;
        double dfz = 1.0d / ray.z;

        double t1 = box.minX * dfx;
        double t2 = box.maxX * dfx;
        double t3 = box.minY * dfy;
        double t4 = box.maxY * dfy;
        double t5 = box.minZ * dfz;
        double t6 = box.maxZ * dfz;

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if(tmax < 0) return -1;
        if(tmin > tmax) return -1;
        if(tmin > length) return -1;

        return tmin;
    }

    public static boolean coneAxisIntersectsBox(Box box, Vec3d coneAxisD, double coneLength) {
        return lineSegmentIntersectsBox(box, coneAxisD, coneLength) >= 0;
    }

    public static int computeCandidatesOnBoxEdges(Vec3d coneDirectionD, double coneLength, Vec3d[] vertices, IntIntPair[] edges, double[] pmin, double[] pmax, int numCandidates, IntIntPair[] candidates) {
        for(int i = 0; i < 8; i++) {
            double h = coneDirectionD.dotProduct(vertices[i]);
            pmin[i] = 0 - h;
            pmax[i] = h - coneLength;
        }

        int v0 = 8;
        int v1 = 20;
        for(int i = 0; i < 12; i++, v0++, v1++) {
            IntIntPair edge = edges[i];

            double p0Min = pmin[edge.firstInt()];
            double p1Min = pmin[edge.secondInt()];
            boolean clipMin = (p0Min < 0 && p1Min > 0) || (p0Min > 0 && p1Min < 0);
            if(clipMin) {
                Vec3d v = vertices[edge.firstInt()].multiply(p1Min)
                        .subtract(vertices[edge.secondInt()].multiply(p0Min));
                double d = p1Min - p0Min;
                vertices[v0] = new Vec3d(v.x / d, v.y / d, v.z / d);
            }

            double p0Max = pmax[edge.firstInt()];
            double p1Max = pmax[edge.secondInt()];
            boolean clipMax = (p0Max < 0 && p1Max > 0) || (p0Max > 0 && p1Max < 0);
            if(clipMax) {
                Vec3d v = vertices[edge.firstInt()].multiply(p1Max)
                        .subtract(vertices[edge.secondInt()].multiply(p0Max));
                double d = p1Max - p0Max;
                vertices[v1] = new Vec3d(v.x / d, v.y / d, v.z / d);
            }

            if(clipMin) {
                if(clipMax) {
                    candidates[numCandidates] = IntIntPair.of(v0, v1);
                } else {
                    if(p0Min < 0) {
                        candidates[numCandidates] = IntIntPair.of(edge.firstInt(), v0);
                    } else {
                        candidates[numCandidates] = IntIntPair.of(edge.secondInt(), v0);
                    }
                }
                numCandidates++;
            } else if(clipMax) {
                if(p0Max < 0) {
                    candidates[numCandidates] = IntIntPair.of(edge.firstInt(), v1);
                } else {
                    candidates[numCandidates] = IntIntPair.of(edge.secondInt(), v1);
                }
                numCandidates++;
            } else {
                if(p0Min <= 0 && p1Min <= 0 && p0Max <= 0 && p1Max <= 0) {
                    candidates[numCandidates] = edge;
                    numCandidates++;
                }
            }
        }

        return numCandidates;
    }

    @FunctionalInterface
    private static interface Configurator {
        int apply(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face);
    }

    private static final Configurator[] configuration = new Configurator[] {
            ConeBoxIntersection::NNNN_0,
            ConeBoxIntersection::NNNZ_1,
            ConeBoxIntersection::NNNP_2,
            ConeBoxIntersection::NNZN_3,
            ConeBoxIntersection::NNZZ_4,
            ConeBoxIntersection::NNZP_5,
            ConeBoxIntersection::NNPN_6,
            ConeBoxIntersection::NNPZ_7,
            ConeBoxIntersection::NNPP_8,
            ConeBoxIntersection::NZNN_9,
            ConeBoxIntersection::NZNZ_10,
            ConeBoxIntersection::NZNP_11,
            ConeBoxIntersection::NZZN_12,
            ConeBoxIntersection::NZZZ_13,
            ConeBoxIntersection::NZZP_14,
            ConeBoxIntersection::NZPN_15,
            ConeBoxIntersection::NZPZ_16,
            ConeBoxIntersection::NZPP_17,
            ConeBoxIntersection::NPNN_18,
            ConeBoxIntersection::NPNZ_19,
            ConeBoxIntersection::NPNP_20,
            ConeBoxIntersection::NPZN_21,
            ConeBoxIntersection::NPZZ_22,
            ConeBoxIntersection::NPZP_23,
            ConeBoxIntersection::NPPN_24,
            ConeBoxIntersection::NPPZ_25,
            ConeBoxIntersection::NPPP_26,
            ConeBoxIntersection::ZNNN_27,
            ConeBoxIntersection::ZNNZ_28,
            ConeBoxIntersection::ZNNP_29,
            ConeBoxIntersection::ZNZN_30,
            ConeBoxIntersection::ZNZZ_31,
            ConeBoxIntersection::ZNZP_32,
            ConeBoxIntersection::ZNPN_33,
            ConeBoxIntersection::ZNPZ_34,
            ConeBoxIntersection::ZNPP_35,
            ConeBoxIntersection::ZZNN_36,
            ConeBoxIntersection::ZZNZ_37,
            ConeBoxIntersection::ZZNP_38,
            ConeBoxIntersection::ZZZN_39,
            ConeBoxIntersection::ZZZZ_40,
            ConeBoxIntersection::ZZZP_41,
            ConeBoxIntersection::ZZPN_42,
            ConeBoxIntersection::ZZPZ_43,
            ConeBoxIntersection::ZZPP_44,
            ConeBoxIntersection::ZPNN_45,
            ConeBoxIntersection::ZPNZ_46,
            ConeBoxIntersection::ZPNP_47,
            ConeBoxIntersection::ZPZN_48,
            ConeBoxIntersection::ZPZZ_49,
            ConeBoxIntersection::ZPZP_50,
            ConeBoxIntersection::ZPPN_51,
            ConeBoxIntersection::ZPPZ_52,
            ConeBoxIntersection::ZPPP_53,
            ConeBoxIntersection::PNNN_54,
            ConeBoxIntersection::PNNZ_55,
            ConeBoxIntersection::PNNP_56,
            ConeBoxIntersection::PNZN_57,
            ConeBoxIntersection::PNZZ_58,
            ConeBoxIntersection::PNZP_59,
            ConeBoxIntersection::PNPN_60,
            ConeBoxIntersection::PNPZ_61,
            ConeBoxIntersection::PNPP_62,
            ConeBoxIntersection::PZNN_63,
            ConeBoxIntersection::PZNZ_64,
            ConeBoxIntersection::PZNP_65,
            ConeBoxIntersection::PZZN_66,
            ConeBoxIntersection::PZZZ_67,
            ConeBoxIntersection::PZZP_68,
            ConeBoxIntersection::PZPN_69,
            ConeBoxIntersection::PZPZ_70,
            ConeBoxIntersection::PZPP_71,
            ConeBoxIntersection::PPNN_72,
            ConeBoxIntersection::PPNZ_73,
            ConeBoxIntersection::PPNP_74,
            ConeBoxIntersection::PPZN_75,
            ConeBoxIntersection::PPZZ_76,
            ConeBoxIntersection::PPZP_77,
            ConeBoxIntersection::PPPN_78,
            ConeBoxIntersection::PPPZ_79,
            ConeBoxIntersection::PPPP_80
    };

    public static int computeCanditatesOnBoxFaces(Vec3d[] vertices, Face[] faces, double[] pmin, double[] pmax, int numCandidates, IntIntPair[] candidates) {
        double p0;
        double p1;
        double p2;
        double p3;
        int b0;
        int b1;
        int b2;
        int b3;
        int index;

        for(int i = 0; i < 6; i++) {
            Face face = faces[i];

            p0 = pmin[face.v0];
            p1 = pmin[face.v1];
            p2 = pmin[face.v2];
            p3 = pmin[face.v3];
            b0 = (p0 < 0 ? 0 : (p0 > 0 ? 2 : 1));
            b1 = (p1 < 0 ? 0 : (p1 > 0 ? 2 : 1));
            b2 = (p2 < 0 ? 0 : (p2 > 0 ? 2 : 1));
            b3 = (p3 < 0 ? 0 : (p3 > 0 ? 2 : 1));
            index = b3 + 3 * (b2 + 3 * (b1 + 3 * (b0)));
            numCandidates = configuration[index].apply(vertices, numCandidates, candidates, 8, face);

            p0 = pmax[face.v0];
            p1 = pmax[face.v1];
            p2 = pmax[face.v2];
            p3 = pmax[face.v3];
            b0 = (p0 < 0 ? 0 : (p0 > 0 ? 2 : 1));
            b1 = (p1 < 0 ? 0 : (p1 > 0 ? 2 : 1));
            b2 = (p2 < 0 ? 0 : (p2 > 0 ? 2 : 1));
            b3 = (p3 < 0 ? 0 : (p3 > 0 ? 2 : 1));
            index = b3 + 3 * (b2 + 3 * (b1 + 3 * (b0)));
            numCandidates = configuration[index].apply(vertices, numCandidates, candidates, 20, face);
        }
        return numCandidates;
    }

    public static boolean candidatesHavePointInsideCone(Vec3d coneDirectionD, double coneCosAngle, Vec3d[] vertices, int numCandidates, IntIntPair[] candidates) {
        for(int i = 0; i < numCandidates; i++) {
            IntIntPair edge = candidates[i];
            if(hasPointInsideCone(coneDirectionD, coneCosAngle, vertices[edge.firstInt()], vertices[edge.secondInt()])) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPointInsideCone(Vec3d coneDirectionD, double coneCosAngle, Vec3d p0, Vec3d p1) {
        double g = coneDirectionD.dotProduct(p0) - (coneCosAngle * p0.length());
        if(g > 0) return true;

        g = coneDirectionD.dotProduct(p1) - (coneCosAngle * p1.length());
        if(g > 0) return true;

        Vec3d e = p1.subtract(p0);
        Vec3d crossP0U = p0.crossProduct(coneDirectionD);
        Vec3d crossP0E = p0.crossProduct(e);
        double dphi0 = crossP0E.dotProduct(crossP0U);
        if(dphi0 > 0) {
            Vec3d crossP1U = p1.crossProduct(coneDirectionD);
            double dphi1 = crossP0E.dotProduct(crossP1U);
            if(dphi1 < 0) {
                double t = dphi0 / (dphi0 - dphi1);
                Vec3d pMax = p0.add(e.multiply(t));
                g = coneDirectionD.dotProduct(pMax) - (coneCosAngle * pMax.length());
                if(g > 0) return true;
            }
        }

        return false;
    }

    private static final IntIntPair[] globalEdges = new IntIntPair[] {
            IntIntPair.of(0, 1),
            IntIntPair.of(1, 3),
            IntIntPair.of(2, 3),
            IntIntPair.of(0, 2),
            IntIntPair.of(4, 5),
            IntIntPair.of(5, 7),
            IntIntPair.of(6, 7),
            IntIntPair.of(4, 6),
            IntIntPair.of(0, 4),
            IntIntPair.of(1, 5),
            IntIntPair.of(3, 7),
            IntIntPair.of(2, 6)
    };

    private static final Face[] globalFaces = new Face[] {
            new Face(0, 4, 6, 2, 8, 7, 11, 3),
            new Face(1, 3, 7, 5, 1, 10, 5, 9),
            new Face(0, 1, 5, 4, 0, 9, 4, 8),
            new Face(2, 6, 7, 3, 11, 6, 10, 2),
            new Face(0, 2, 3, 1, 3, 2, 1, 0),
            new Face(4, 5, 7, 6, 4, 5, 6, 7)
    };

    public static boolean intersectsAligned(Box box, Vec3d coneDirectionD, double coneLength, double coneAngle) {
        double[] boxHeightInterval = computeBoxHeightInterval(box, coneDirectionD);
        double boxMinHeight = boxHeightInterval[0];
        double boxMaxHeight = boxHeightInterval[1];

        if(boxMinHeight >= coneLength || boxMaxHeight <= 0) return false;

        if(coneAxisIntersectsBox(box, coneDirectionD, coneLength)) return true;

        double coneCosAngle = Math.cos(coneAngle);

        Vec3d[] vertices = new Vec3d[32];
        vertices[0] = new Vec3d(box.minX, box.minY, box.minZ);
        vertices[1] = new Vec3d(box.maxX, box.minY, box.minZ);
        vertices[2] = new Vec3d(box.minX, box.maxY, box.minZ);
        vertices[3] = new Vec3d(box.maxX, box.maxY, box.minZ);
        vertices[4] = new Vec3d(box.minX, box.minY, box.maxZ);
        vertices[5] = new Vec3d(box.maxX, box.minY, box.maxZ);
        vertices[6] = new Vec3d(box.minX, box.maxY, box.maxZ);
        vertices[7] = new Vec3d(box.maxX, box.maxY, box.maxZ);

        IntIntPair[] candidates = new IntIntPair[32 * 32];
        if(0d <= boxMinHeight && boxMaxHeight <= coneLength) {
            for(int i = 0; i < 12; i++) {
                candidates[i] = globalEdges[i];
            }
            return candidatesHavePointInsideCone(coneDirectionD, coneCosAngle, vertices, 12, candidates);
        }

        int numCandidates = 0;
        double[] pmin = new double[8];
        double[] pmax = new double[8];

        numCandidates = computeCandidatesOnBoxEdges(coneDirectionD, coneLength, vertices, globalEdges, pmin, pmax, numCandidates, candidates);
        numCandidates = computeCanditatesOnBoxFaces(vertices, globalFaces, pmin, pmax, numCandidates, candidates);

        return candidatesHavePointInsideCone(coneDirectionD, coneCosAngle, vertices, numCandidates, candidates);
    }

    public static boolean intersects(Box box, Vec3d coneVertexV, Vec3d coneDirectionD, double coneLength, double coneAngle) {
        // don't need to dot because Box is AABB
        // don't need to make new box
        Box xfrmBox = box.offset(coneVertexV.negate());

        // don't need to make xfrmCone because Box is AABB
        return intersectsAligned(xfrmBox, coneDirectionD, coneLength, coneAngle);
    }

    private static int NNNN_0(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NNNZ_1(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NNNP_2(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, index + face.e3);
        return 1 + numCandidates;
    }

    private static int NNZN_3(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NNZZ_4(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NNZP_5(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v2, index + face.e3);
        return 1 + numCandidates;
    }

    private static int NNPN_6(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e1, index + face.e2);
        return 1 + numCandidates;
    }

    private static int NNPZ_7(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e1, face.v3);
        return 1 + numCandidates;
    }

    private static int NNPP_8(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e1, index + face.e3);
        return 1 + numCandidates;
    }

    private static int NZNN_9(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NZNZ_10(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NZNP_11(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, face.v3);
        candidates[1 + numCandidates] = IntIntPair.of(index + face.e3, face.v3);
        return 2 + numCandidates;
    }

    private static int NZZN_12(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NZZZ_13(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int NZZP_14(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v2, face.v3);
        candidates[1 + numCandidates] = IntIntPair.of(index + face.e3, face.v3);
        return 2 + numCandidates;
    }

    private static int NZPN_15(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, face.v1);
        return 1 + numCandidates;
    }

    private static int NZPZ_16(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v1, face.v3);
        return 1 + numCandidates;
    }

    private static int NZPP_17(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, face.v1);
        return 1 + numCandidates;
    }

    private static int NPNN_18(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, index + face.e1);
        return 1 + numCandidates;
    }

    private static int NPNZ_19(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(index + face.e1, face.v1);
        return 2 + numCandidates;
    }

    private static int NPNP_20(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(index + face.e1, face.v1);
        candidates[2 + numCandidates] = IntIntPair.of(index + face.e2, face.v3);
        candidates[3 + numCandidates] = IntIntPair.of(index + face.e3, face.v3);
        return 4 + numCandidates;
    }

    private static int NPZN_21(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, face.v2);
        return 1 + numCandidates;
    }

    private static int NPZZ_22(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(face.v1, face.v2);
        return 2 + numCandidates;
    }

    private static int NPZP_23(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(face.v1, face.v2);
        candidates[2 + numCandidates] = IntIntPair.of(index + face.e3, face.v2);
        candidates[3 + numCandidates] = IntIntPair.of(face.v2, face.v3);
        return 4 + numCandidates;
    }

    private static int NPPN_24(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, index + face.e2);
        return 1 + numCandidates;
    }

    private static int NPPZ_25(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, face.v3);
        return 1 + numCandidates;
    }

    private static int NPPP_26(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, index + face.e3);
        return 1 + numCandidates;
    }

    private static int ZNNN_27(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZNNZ_28(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZNNP_29(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, face.v0);
        return 1 + numCandidates;
    }

    private static int ZNZN_30(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZNZZ_31(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZNZP_32(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v2);
        return 1 + numCandidates;
    }

    private static int ZNPN_33(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e1, face.v2);
        candidates[1 + numCandidates] = IntIntPair.of(index + face.e2, face.v2);
        return 2 + numCandidates;
    }

    private static int ZNPZ_34(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e1, face.v2);
        candidates[1 + numCandidates] = IntIntPair.of(face.v2, face.v3);
        return 2 + numCandidates;
    }

    private static int ZNPP_35(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, index + face.e1);
        return 1 + numCandidates;
    }

    private static int ZZNN_36(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZZNZ_37(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZZNP_38(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v3);
        candidates[1 + numCandidates] = IntIntPair.of(face.v3, index + face.e2);
        return 2 + numCandidates;
    }

    private static int ZZZN_39(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZZZZ_40(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZZZP_41(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v3);
        candidates[1 + numCandidates] = IntIntPair.of(face.v3, face.v2);
        return 2 + numCandidates;
    }

    private static int ZZPN_42(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v1, face.v2);
        candidates[1 + numCandidates] = IntIntPair.of(face.v2, index + face.e2);
        return 2 + numCandidates;
    }

    private static int ZZPZ_43(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v1, face.v2);
        candidates[1 + numCandidates] = IntIntPair.of(face.v2, face.v3);
        return 2 + numCandidates;
    }

    private static int ZZPP_44(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZPNN_45(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, index + face.e1);
        return 1 + numCandidates;
    }

    private static int ZPNZ_46(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(face.v1, index + face.e1);
        return 2 + numCandidates;
    }

    private static int ZPNP_47(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(face.v1, index + face.e1);
        candidates[2 + numCandidates] = IntIntPair.of(index + face.e2, face.v3);
        candidates[3 + numCandidates] = IntIntPair.of(face.v3, face.v0);
        return 4 + numCandidates;
    }

    private static int ZPZN_48(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v2);
        return 1 + numCandidates;
    }

    private static int ZPZZ_49(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, face.v1);
        candidates[1 + numCandidates] = IntIntPair.of(face.v1, face.v2);
        return 2 + numCandidates;
    }

    private static int ZPZP_50(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZPPN_51(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v0, index + face.e2);
        return 1 + numCandidates;
    }

    private static int ZPPZ_52(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int ZPPP_53(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PNNN_54(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, index + face.e0);
        return 1 + numCandidates;
    }

    private static int PNNZ_55(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v3, index + face.e0);
        return 1 + numCandidates;
    }

    private static int PNNP_56(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, index + face.e0);
        return 1 + numCandidates;
    }

    private static int PNZN_57(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, face.v0);
        candidates[1 + numCandidates] = IntIntPair.of(face.v0, index + face.e0);
        return 2 + numCandidates;
    }

    private static int PNZZ_58(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v3, face.v0);
        candidates[1 + numCandidates] = IntIntPair.of(face.v0, index + face.e0);
        return 2 + numCandidates;
    }

    private static int PNZP_59(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v2, index + face.e0);
        return 1 + numCandidates;
    }

    private static int PNPN_60(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, face.v0);
        candidates[1 + numCandidates] = IntIntPair.of(face.v0, index + face.e0);
        candidates[2 + numCandidates] = IntIntPair.of(index + face.e1, face.v2);
        candidates[3 + numCandidates] = IntIntPair.of(face.v2, index + face.e2);
        return 4 + numCandidates;
    }

    private static int PNPZ_61(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v3, face.v0);
        candidates[1 + numCandidates] = IntIntPair.of(face.v0, index + face.e0);
        candidates[2 + numCandidates] = IntIntPair.of(index + face.e1, face.v2);
        candidates[3 + numCandidates] = IntIntPair.of(face.v2, face.v3);
        return 4 + numCandidates;
    }

    private static int PNPP_62(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e0, index + face.e1);
        return 1 + numCandidates;
    }

    private static int PZNN_63(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, face.v1);
        return 1 + numCandidates;
    }

    private static int PZNZ_64(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v3, face.v1);
        return 1 + numCandidates;
    }

    private static int PZNP_65(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, face.v1);
        return 1 + numCandidates;
    }

    private static int PZZN_66(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, face.v0);
        candidates[1 + numCandidates] = IntIntPair.of(face.v0, face.v1);
        return 2 + numCandidates;
    }

    private static int PZZZ_67(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PZZP_68(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PZPN_69(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, face.v0);
        candidates[1 + numCandidates] = IntIntPair.of(face.v0, face.v1);
        candidates[2 + numCandidates] = IntIntPair.of(face.v1, face.v2);
        candidates[3 + numCandidates] = IntIntPair.of(face.v2, index + face.e2);
        return 4 + numCandidates;
    }

    private static int PZPZ_70(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PZPP_71(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PPNN_72(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, index + face.e1);
        return 1 + numCandidates;
    }

    private static int PPNZ_73(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(face.v3, index + face.e1);
        return 1 + numCandidates;
    }

    private static int PPNP_74(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, index + face.e1);
        return 1 + numCandidates;
    }

    private static int PPZN_75(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e2, face.v2);
        return 1 + numCandidates;
    }

    private static int PPZZ_76(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PPZP_77(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PPPN_78(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        candidates[numCandidates] = IntIntPair.of(index + face.e3, index + face.e2);
        return 1 + numCandidates;
    }

    private static int PPPZ_79(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

    private static int PPPP_80(Vec3d[] vertices, int numCandidates, IntIntPair[] candidates, int index, Face face) {
        return numCandidates;
    }

}
