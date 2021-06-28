package it.unive.lisa.test.stripes.simplifier;

import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-17
 * @since version date
 */
class OldNormalizer {

    public static Satisfiability normalizeCondition(
        final SymbolicExpression expression,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> newConstraints
    ) {
        return OldNormalizer.normalizeCondition(expression, oldConstraints, newConstraints, false);
    }

    private static Satisfiability normalizeCondition(
        final SymbolicExpression expression,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> oldConstraints,
        @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> newConstraints,
        final boolean complement
    ) {
        //noinspection ChainOfInstanceofChecks
        if (
            (expression instanceof UnaryExpression unaryExpression) &&
            (unaryExpression.getOperator() == UnaryOperator.LOGICAL_NOT)
        ) {
            final Satisfiability result = OldNormalizer.normalizeCondition(
                unaryExpression.getExpression(),
                oldConstraints,
                newConstraints,
                !complement
            );
            if ((result == Satisfiability.SATISFIED) || (result == Satisfiability.NOT_SATISFIED)) {
                newConstraints.clear();
            }
            return result;
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            final var operator = binaryExpression.getOperator();
            if (
                (operator == BinaryOperator.LOGICAL_AND) || (operator == BinaryOperator.LOGICAL_OR)
            ) {
                final Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> leftConstraints = new HashMap<>();
                final Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> rightConstraints = new HashMap<>();
                final Satisfiability leftCondition = OldNormalizer.normalizeCondition(
                    binaryExpression.getLeft(),
                    oldConstraints,
                    leftConstraints,
                    complement
                );
                final Satisfiability rightCondition = OldNormalizer.normalizeCondition(
                    binaryExpression.getRight(),
                    oldConstraints,
                    rightConstraints,
                    complement
                );

                if (
                    ((operator == BinaryOperator.LOGICAL_AND) && !complement) ||
                    ((operator == BinaryOperator.LOGICAL_OR) && complement)
                ) {
                    if (leftCondition == Satisfiability.SATISFIED) {
                        if (rightCondition == Satisfiability.SATISFIED) {
                            return Satisfiability.SATISFIED;
                        }
                        if (rightCondition == Satisfiability.NOT_SATISFIED) {
                            return Satisfiability.NOT_SATISFIED;
                        }
                        if (rightCondition == Satisfiability.UNKNOWN) {
                            OldUtils.mergeConstraints(newConstraints, rightConstraints);
                            return Satisfiability.UNKNOWN;
                        }
                    }
                    if (leftCondition == Satisfiability.NOT_SATISFIED) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    if (rightCondition == Satisfiability.NOT_SATISFIED) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    OldUtils.mergeConstraints(newConstraints, leftConstraints);
                    OldUtils.mergeConstraints(newConstraints, rightConstraints);
                    return Satisfiability.UNKNOWN;
                }
                //if ((operator == BinaryOperator.LOGICAL_OR && !complement) || (operator == BinaryOperator.LOGICAL_AND && complement)) {
                if (leftCondition == Satisfiability.SATISFIED) {
                    return Satisfiability.SATISFIED;
                }
                if (leftCondition == Satisfiability.NOT_SATISFIED) {
                    OldUtils.mergeConstraints(newConstraints, rightConstraints);
                    return rightCondition;
                }
                if (rightCondition == Satisfiability.SATISFIED) {
                    return Satisfiability.SATISFIED;
                }
                if (rightCondition == Satisfiability.NOT_SATISFIED) {
                    OldUtils.mergeConstraints(newConstraints, leftConstraints);
                }
                return Satisfiability.UNKNOWN;
                // }
            }
            return OldNormalizer.normalizeExpression(
                binaryExpression,
                oldConstraints,
                newConstraints,
                complement
            );
        }
        return Satisfiability.UNKNOWN;
    }

    private static Satisfiability normalizeExpression(
        final BinaryExpression expression,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> newConstraints,
        final boolean complement
    ) {
        final OldPolynomial leftPolynomial = OldSimplifier2.simplify(expression.getLeft(), 3);
        final OldPolynomial rightPolynomial = OldSimplifier2.simplify(expression.getRight(), 3);
        final OldPolynomial one = new OldPolynomial(3, 1);
        if (leftPolynomial.isValid() && rightPolynomial.isValid()) {
            final BinaryOperator operator = expression.getOperator();
            if (
                ((operator == BinaryOperator.COMPARISON_EQ) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_NE) && complement)
            ) {
                final OldPolynomial first = new OldPolynomial(leftPolynomial);
                first.subtract(rightPolynomial);
                first.add(one);
                final OldPolynomial second = new OldPolynomial(rightPolynomial);
                second.subtract(leftPolynomial);
                second.add(one);

                final Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> firstMap = new HashMap<>();
                final Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> secondMap = new HashMap<>();

                final Satisfiability result1 = OldNormalizer.normalizePolynomial(
                    first,
                    oldConstraints,
                    firstMap
                );
                final Satisfiability result2 = OldNormalizer.normalizePolynomial(
                    second,
                    oldConstraints,
                    secondMap
                );

                if (result1 == Satisfiability.SATISFIED) {
                    if (result2 == Satisfiability.SATISFIED) {
                        return Satisfiability.SATISFIED;
                    }
                    if (result2 == Satisfiability.NOT_SATISFIED) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    if (result2 == Satisfiability.UNKNOWN) {
                        OldUtils.mergeConstraints(newConstraints, firstMap);
                        return Satisfiability.UNKNOWN;
                    }
                }
                if (result1 == Satisfiability.NOT_SATISFIED) {
                    return Satisfiability.NOT_SATISFIED;
                }
                if (result1 == Satisfiability.UNKNOWN) {
                    if (result2 == Satisfiability.NOT_SATISFIED) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    OldUtils.mergeConstraints(newConstraints, firstMap);
                    OldUtils.mergeConstraints(newConstraints, secondMap);
                    return Satisfiability.UNKNOWN;
                }
            }
            if (
                ((operator == BinaryOperator.COMPARISON_NE) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_EQ) && complement)
            ) {
                final OldPolynomial first = new OldPolynomial(leftPolynomial);
                first.subtract(rightPolynomial);
                final OldPolynomial second = new OldPolynomial(rightPolynomial);
                second.subtract(leftPolynomial);

                final Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> firstMap = new HashMap<>();
                final Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> secondMap = new HashMap<>();

                final Satisfiability result1 = OldNormalizer.normalizePolynomial(
                    first,
                    oldConstraints,
                    firstMap
                );
                final Satisfiability result2 = OldNormalizer.normalizePolynomial(
                    second,
                    oldConstraints,
                    secondMap
                );

                if (result1 == Satisfiability.SATISFIED) {
                    return Satisfiability.SATISFIED;
                }
                if (result1 == Satisfiability.NOT_SATISFIED) {
                    OldUtils.mergeConstraints(newConstraints, secondMap);
                    return result2;
                }
                if (result1 == Satisfiability.UNKNOWN) {
                    if (result2 == Satisfiability.SATISFIED) {
                        return Satisfiability.SATISFIED;
                    }
                    if (result2 == Satisfiability.NOT_SATISFIED) {
                        OldUtils.mergeConstraints(newConstraints, firstMap);
                    }
                    return result2;
                }
            }
            OldPolynomial polynomialToCheck = null;
            if (
                ((operator == BinaryOperator.COMPARISON_LT) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_GE) && complement)
            ) {
                // !(i >= arr_len)
                // i < arr_len --> arr_len > i --> arr_len - i > 0

                // a < b ---> b > a ---> b - a > 0
                polynomialToCheck = new OldPolynomial(rightPolynomial);
                polynomialToCheck.subtract(leftPolynomial);
            }
            if (
                ((operator == BinaryOperator.COMPARISON_GE) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_LT) && complement)
            ) {
                // a >= b --> a-b >= 0 --> a-b > -1 --> a - b + 1 > 0

                polynomialToCheck = new OldPolynomial(leftPolynomial);
                polynomialToCheck.subtract(rightPolynomial);
                polynomialToCheck.add(one);
            }
            if (
                ((operator == BinaryOperator.COMPARISON_LE) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_GT) && complement)
            ) {
                // a <= b --> b >= a ---> b - a >= 0 --> b - a > -1 --> b-a+1 > 0
                polynomialToCheck = new OldPolynomial(rightPolynomial);
                polynomialToCheck.subtract(leftPolynomial);
                polynomialToCheck.add(one);
            }
            if (
                ((operator == BinaryOperator.COMPARISON_GT) && !complement) ||
                ((expression.getOperator() == BinaryOperator.COMPARISON_LE) && complement)
            ) {
                // a > b --> a - b > 0
                polynomialToCheck = new OldPolynomial(leftPolynomial);
                polynomialToCheck.subtract(rightPolynomial);
            }
            if (polynomialToCheck != null) {
                return OldNormalizer.normalizePolynomial(
                    polynomialToCheck,
                    oldConstraints,
                    newConstraints
                );
            }
        }
        return Satisfiability.UNKNOWN;
    }

    private static Satisfiability normalizePolynomial(
        final OldPolynomial polynomialToNormalize,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> newConstraints
    ) {
        if (polynomialToNormalize.isValid() && (polynomialToNormalize.getSize() >= 2)) {
            final OldMonomial a = polynomialToNormalize.getMonomial(0);
            final OldMonomial b = polynomialToNormalize.getMonomial(1);
            @Nullable
            final OldMonomial c = (polynomialToNormalize.getSize() == 2)
                ? null
                : polynomialToNormalize.getMonomial(2);
            final int constant = polynomialToNormalize.getConstant();

            if ((a != null) && (b != null)) {
                if (c == null) {
                    if (a.getCoefficient() == 1) {
                        final Satisfiability result = OldNormalizer.checkExpression(
                            a.getVariable(),
                            b.getVariable(),
                            null,
                            -b.getCoefficient(),
                            -constant,
                            oldConstraints,
                            newConstraints
                        );
                        if ((result != Satisfiability.SATISFIED) && (b.getCoefficient() == 1)) {
                            final Satisfiability result2 = OldNormalizer.checkExpression(
                                b.getVariable(),
                                a.getVariable(),
                                null,
                                -a.getCoefficient(),
                                -constant,
                                oldConstraints,
                                newConstraints
                            );
                            
                            if (
                                (
                                    (result == Satisfiability.NOT_SATISFIED) &&
                                    (result2 != Satisfiability.UNKNOWN)
                                ) ||
                                (
                                    (result == Satisfiability.UNKNOWN) &&
                                    (result2 != Satisfiability.NOT_SATISFIED)
                                )
                            ) {
                                newConstraints.clear();
                            }
                            if (result != Satisfiability.UNKNOWN) {
                                return result2;
                            }
                        }
                        return result;
                    } // end a.getCoeff() == 1
                    if (b.getCoefficient() == 1) {
                        return OldNormalizer.checkExpression(
                            b.getVariable(),
                            a.getVariable(),
                            null,
                            -a.getCoefficient(),
                            -constant,
                            oldConstraints,
                            newConstraints
                        );
                    }
                    return Satisfiability.UNKNOWN;
                }
                // end c == null
                if (a.getCoefficient() == 1) {
                    if (b.getCoefficient() == c.getCoefficient()) {
                        final Satisfiability result1 = OldNormalizer.checkExpression(
                            a.getVariable(),
                            b.getVariable(),
                            c.getVariable(),
                            -b.getCoefficient(),
                            -constant,
                            oldConstraints,
                            newConstraints
                        );
                        if ((result1 != Satisfiability.SATISFIED) && (b.getCoefficient() == 1)) {
                            final Satisfiability result2 = OldNormalizer.checkExpression(
                                b.getVariable(),
                                a.getVariable(),
                                c.getVariable(),
                                -a.getCoefficient(),
                                -constant,
                                oldConstraints,
                                newConstraints
                            );
                            if (result2 != Satisfiability.SATISFIED) {
                                final Satisfiability result3 = OldNormalizer.checkExpression(
                                    c.getVariable(),
                                    a.getVariable(),
                                    b.getVariable(),
                                    -a.getCoefficient(),
                                    -constant,
                                    oldConstraints,
                                    newConstraints
                                );
                                if (
                                    (
                                        (result1 == Satisfiability.UNKNOWN) &&
                                        (
                                            (result2 == Satisfiability.UNKNOWN) ||
                                            (result3 == Satisfiability.UNKNOWN)
                                        )
                                    ) ||
                                    (
                                        (result2 == Satisfiability.UNKNOWN) &&
                                        (result3 == Satisfiability.UNKNOWN)
                                    ) ||
                                    (result3 == Satisfiability.SATISFIED)
                                ) {
                                    newConstraints.clear();
                                }
                                if (
                                    (
                                        (result1 != Satisfiability.UNKNOWN) &&
                                        (result2 != Satisfiability.UNKNOWN)
                                    ) ||
                                    (result3 == Satisfiability.SATISFIED)
                                ) {
                                    return result3;
                                }
                            }
                            if (
                                (result1 == Satisfiability.UNKNOWN) &&
                                (result2 == Satisfiability.SATISFIED)
                            ) {
                                newConstraints.clear();
                            }
                            if (
                                (result1 != Satisfiability.UNKNOWN) ||
                                (result2 == Satisfiability.SATISFIED)
                            ) {
                                return result2;
                            }
                            return result1;
                        }
                        return result1;
                    }
                    return Satisfiability.UNKNOWN;
                } // end a.getCoeff() == 1
                if ((b.getCoefficient() == 1) && (a.getCoefficient() == c.getCoefficient())) {
                    return OldNormalizer.checkExpression(
                        b.getVariable(),
                        a.getVariable(),
                        c.getVariable(),
                        -a.getCoefficient(),
                        -constant,
                        oldConstraints,
                        newConstraints
                    );
                }
                if ((c.getCoefficient() == 1) && (a.getCoefficient() == b.getCoefficient())) {
                    return OldNormalizer.checkExpression(
                        c.getVariable(),
                        a.getVariable(),
                        b.getVariable(),
                        -a.getCoefficient(),
                        -constant,
                        oldConstraints,
                        newConstraints
                    );
                }
            }
        }
        return Satisfiability.UNKNOWN;
    }

    private static Satisfiability checkExpression(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull OldConstraint>> oldConstraints,
        final @Nullable Map<? super @NotNull Variable, @NotNull Set<@NotNull OldConstraint>> newConstraints
    ) {
        final boolean normalDirectionSatified = OldNormalizer.checkVariableConstraint(
            x,
            y,
            z,
            k1,
            k2,
            oldConstraints
        );
        if (normalDirectionSatified) {
            return Satisfiability.SATISFIED;
        }
        if ((k1 == 1) && (z == null)) {
            final boolean inverseDirectionSatisfied = OldNormalizer.checkVariableConstraint(
                y,
                x,
                z,
                k1,
                -k2-1,
                oldConstraints
            );
            if (inverseDirectionSatisfied) {
                return Satisfiability.NOT_SATISFIED;
            }
        }
        if (newConstraints != null) {
            OldUtils.addConstraint(newConstraints, x, new OldConstraint(y, z, k1, k2));
        }

        return Satisfiability.UNKNOWN;
    }

    private static boolean checkVariableConstraint(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2,
        final @NotNull Map<@NotNull Variable, ? extends @NotNull Set<@NotNull OldConstraint>> oldConstraints
    ) {
        // arr_len - 1*(i) > 0
        // {i=[(array_length, ⊥, 1, -2)], array_length=[(i, ⊥, 1, 0)]}

        // arr_len(i, _, 1, >=0)

        // i >= arr_len -----> i - arr_len >= 0 ---> i-arr_len > -1
        // i - 1*arr_len > 1

        @Nullable
        final Set<@NotNull OldConstraint> constraints = oldConstraints.get(x);
        if (constraints != null) {
            for (@NotNull final OldConstraint constraint : constraints) {
                if (
                    constraint.getX().equals(y) &&
                    Objects.equals(constraint.getY(), z) &&
                    (constraint.getK1() == k1) &&
                    (constraint.getK2() >= k2)
                ) {
                    return true;
                    // a -> (b, _, 1, 7) --> a -b>7
                    // a -> (b, _, 1, >=10)
                }
            }
        }

        /*
         * a = b+6 ::::::: {a -> [b,_,1,5], b -> {a, _, -1, -7}} ----> a-1(b)>=6 --> b - a > -7
         * if (b > a) :::::::: b - 1(a) > 0   b -> (a, _, 1, >=0)
         * // b <= a --> a >= b --> a > b-1   a - 1*b > -1    a -> (b, _, 1, >=-1)
         *
         *
         */

        // array_len-1*i > 0

        // arr_len-1*i>-1
        return false;
    }
}
