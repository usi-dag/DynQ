package ch.usi.inf.dag.dynq_r.parser.tpch_hardcoded_plans;

import ch.usi.inf.dag.dynq.runtime.objects.api.APISessionManagement;
import org.apache.calcite.rel.RelNode;

public class HardCodedPlanGetter {

    public static RelNode getPlan(int query, APISessionManagement sessionManagement) {
        switch (query) {
            case 20: return PlanQ20.plan(sessionManagement);
            case 21: return PlanQ21.plan(sessionManagement);
        }
        return null;
    }


}
