package ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.functions;

import ch.usi.inf.dag.dynq.language.nodes.utils.TruffleBoundaryUtils;
import ch.usi.inf.dag.dynq.runtime.objects.api.udf.SqlJavaUDFunction2;
import ch.usi.inf.dag.dynq_js.language.nodes.sql.expressions.JSReadElementNodeFactory;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.access.ReadElementNode;
import com.oracle.truffle.js.runtime.JSContext;

@ImportStatic({JSReadElementNodeFactory.class})
public abstract class HaversineUDFNode extends Node implements SqlJavaUDFunction2 {

    private static final double R = 6371;

    @Child
    private AsDoubleNode asDouble1Node = HaversineUDFNodeGen.AsDoubleNodeGen.create();
    @Child
    private AsDoubleNode asDouble2Node = HaversineUDFNodeGen.AsDoubleNodeGen.create();
    @Child
    private AsDoubleNode asDouble3Node = HaversineUDFNodeGen.AsDoubleNodeGen.create();
    @Child
    private AsDoubleNode asDouble4Node = HaversineUDFNodeGen.AsDoubleNodeGen.create();

    public abstract Double execute(Object fst, Object snd);

    @Specialization
    public Double executeWithLib(Object fst, Object snd,
                                 @Cached(value = "getJSContext()", uncached = "getUncachedRead()") JSContext jsContext,
                                 @Cached(value = "create(jsContext)", uncached = "getUncachedRead()") ReadElementNode readLatitude1Node,
                                 @Cached(value = "create(jsContext)", uncached = "getUncachedRead()") ReadElementNode readLatitude2Node,
                                 @Cached(value = "create(jsContext)", uncached = "getUncachedRead()") ReadElementNode readLongitude1Node,
                                 @Cached(value = "create(jsContext)", uncached = "getUncachedRead()") ReadElementNode readLongitude2Node) {
        try {
            double fstLat = asDouble1Node.execute(readLatitude1Node.executeWithTargetAndIndex(fst, "latitude"));
            double fstLong = asDouble2Node.execute(readLongitude1Node.executeWithTargetAndIndex(fst, "longitude"));
            double sndLat = asDouble3Node.execute(readLatitude2Node.executeWithTargetAndIndex(snd, "latitude"));
            double sndLong = asDouble4Node.execute(readLongitude2Node.executeWithTargetAndIndex(snd, "longitude"));

            double dLat = toRad(sndLat - fstLat);
            double dLon = toRad(sndLong - fstLong);
            double lat1 = toRad(fstLat);
            double lat2 = toRad(sndLat);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            return R * c;

        } catch (UnsupportedMessageException e) {
            return Double.POSITIVE_INFINITY;
        }
    }

    private static double toRad(double num) {
        return num * Math.PI / 180;
    }

    static abstract class AsDoubleNode extends Node {
        abstract double execute(Object value) throws UnsupportedMessageException;

        @Specialization
        double executeDouble(double value) {
            return value;
        }

        @Specialization
        double executeString(String value) {
            return TruffleBoundaryUtils.parseDouble(value);
        }

        @Specialization
        double executeDynObj(DynamicObject obj, @CachedLibrary(limit = "1") InteropLibrary interopLibrary) throws UnsupportedMessageException {
            return interopLibrary.asDouble(obj);
        }
    }
}
