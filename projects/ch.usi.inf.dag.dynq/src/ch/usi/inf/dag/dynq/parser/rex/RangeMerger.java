package ch.usi.inf.dag.dynq.parser.rex;

import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.RexTruffleNode;
import ch.usi.inf.dag.dynq.language.nodes.sql.expressions.untyped.AllRexTruffleNode;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


class RangeMerger {

    static RexTruffleNode maybeMergeRange(RexCall call, RexTruffleNodeVisitor visitor) {
        if(!call.isA(SqlKind.AND)) {
            return null;
        }
        RangeMerger merger = new RangeMerger(visitor);
        List<RexTruffleNode> merged = merger.rewriteCallChildren(call.operands);
        if(merged.size() == call.operands.size()) {
            // no merge at all
            return null;
        }
        if(merged.size() == 1) {
            return merged.get(0);
        }
        return new AllRexTruffleNode(merged.toArray(new RexTruffleNode[0]));
    }

    private RexTruffleNodeVisitor visitor;
    private RangeMerger(RexTruffleNodeVisitor visitor) {
        this.visitor = visitor;
    }


    private List<RexTruffleNode> rewriteCallChildren(List<RexNode> nodes) {
        List<RexTruffleNode> result = new ArrayList<>();
        int size = nodes.size();
        boolean lastIsMerged = false;
        for (int i = 0; i < size - 1; i++) {
            RexNode current = nodes.get(i);
            RexNode next = nodes.get(i + 1);
            RexTruffleNode merged = maybeMerged(current, next);
            if(merged != null) {
                result.add(merged);
                i++;
                if(i == size - 1) {
                    lastIsMerged = true;
                }
            } else {
                result.add(current.accept(visitor));
            }
        }

        if(!lastIsMerged) {
            result.add(nodes.get(nodes.size() - 1).accept(visitor));
        }
        return result;
    }


    private RexTruffleNode maybeMerged(RexNode fst, RexNode snd) {
        if(fst.isA(SqlKind.GREATER_THAN) || fst.isA(SqlKind.GREATER_THAN_OR_EQUAL)) {
            RexCall fstCall = (RexCall) fst;
            if(snd.isA(SqlKind.LESS_THAN) || snd.isA(SqlKind.LESS_THAN_OR_EQUAL)) {
                RexCall sndCall = (RexCall) snd;
                RexNode fstCallArg0 = fstCall.operands.get(0);
                RexNode fstCallArg1 = fstCall.operands.get(1);
                RexNode sndCallArg0 = sndCall.operands.get(0);
                RexNode sndCallArg1 = sndCall.operands.get(1);
                if(fstCallArg0.equals(sndCallArg0) && fstCallArg1 instanceof RexLiteral && sndCallArg1 instanceof RexLiteral) {
                    RexLiteral fstLit = (RexLiteral) fstCallArg1;
                    RexLiteral sndLit = (RexLiteral) sndCallArg1;
                    if(fstLit.getTypeName().equals(sndLit.getTypeName())) {
                        RexTruffleNode merged = merge(fst.getKind(), snd.getKind(),
                                fstCallArg0.accept(visitor), fstLit, sndLit,
                                fstLit.getTypeName());

                        if(visitor.getSession().isDebugEnabled()) {
                            if(merged != null) {
                                System.out.println("Merged: " + sndCallArg1.getType() + " " + fst + " " + snd);
                            } else {
                                System.out.println("cannot merge: " + sndCallArg1.getType() + " " + fst + " " + snd);
                            }
                        }
                        return merged;
                    }
                }
            }
        }
        return null;
    }

    private RexTruffleNode merge(SqlKind fstKind, SqlKind sndKind, RexTruffleNode input, RexLiteral left, RexLiteral right, SqlTypeName typeName) {
        if(fstKind == SqlKind.GREATER_THAN_OR_EQUAL && sndKind == SqlKind.LESS_THAN_OR_EQUAL) {
            return makeLeftOpenRightOpen(input, left, right, typeName);
        } else if(fstKind == SqlKind.GREATER_THAN_OR_EQUAL && sndKind == SqlKind.LESS_THAN) {
            return makeLeftOpenRightClose(input, left, right, typeName);
        } else if(fstKind == SqlKind.GREATER_THAN && sndKind == SqlKind.LESS_THAN) {
            return null;
        } else if(fstKind == SqlKind.GREATER_THAN && sndKind == SqlKind.LESS_THAN_OR_EQUAL) {
            return null;
        }
        return null;
    }


    private RexTruffleNode makeLeftOpenRightOpen(RexTruffleNode input, RexLiteral left, RexLiteral right, SqlTypeName typeName) {
        if(typeName == SqlTypeName.DATE) {
            LocalDate leftVal = RexConversionUtils.asLocalDate(left.getValueAs(Calendar.class));
            LocalDate rightVal = RexConversionUtils.asLocalDate(right.getValueAs(Calendar.class));
            return visitor.makeLeftOpenRightOpenDateRange(input, leftVal, rightVal);
        }
        if(typeName == SqlTypeName.DECIMAL || typeName == SqlTypeName.DOUBLE || typeName == SqlTypeName.FLOAT) {
            double leftVal = left.getValueAs(Double.class);
            double rightVal = right.getValueAs(Double.class);
            return visitor.makeLeftOpenRightOpenDoubleRange(input, leftVal, rightVal);
        }
        return null;
    }

    private RexTruffleNode makeLeftOpenRightClose(RexTruffleNode input, RexLiteral left, RexLiteral right, SqlTypeName typeName) {
        if(typeName == SqlTypeName.DATE) {
            LocalDate leftDate = RexConversionUtils.asLocalDate(left.getValueAs(Calendar.class));
            LocalDate rightDate = RexConversionUtils.asLocalDate(right.getValueAs(Calendar.class));
            return visitor.makeLeftOpenRightCloseDateRange(input, leftDate, rightDate);
        }
        if(typeName == SqlTypeName.DECIMAL || typeName == SqlTypeName.DOUBLE || typeName == SqlTypeName.FLOAT) {
            double leftVal = left.getValueAs(Double.class);
            double rightVal = right.getValueAs(Double.class);
            return visitor.makeLeftOpenRightCloseDoubleRange(input, leftVal, rightVal);
        }
        return null;
    }

}
