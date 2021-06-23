package it.unive.lisa.analysis.nonrelational.value.stripes;

import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.symbolic.value.Variable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-17
 * @since version date
 */
class Normalizer {

    public static Satisfiability normalizeCondition(
        final SymbolicExpression expression,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints
    ) {
        return Normalizer.normalizeCondition(expression, oldConstraints, newConstraints, false);
    }

    private static Satisfiability normalizeCondition(
        final SymbolicExpression expression,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints,
        final boolean complement
    ) {
        //noinspection ChainOfInstanceofChecks
        if (
            (expression instanceof UnaryExpression unaryExpression) &&
            (unaryExpression.getOperator() == UnaryOperator.LOGICAL_NOT)
        ) {
            final Satisfiability result = Normalizer.normalizeCondition(
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
                final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> leftConstraints = new HashMap<>();
                final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> rightConstraints = new HashMap<>();
                final Satisfiability leftCondition = Normalizer.normalizeCondition(
                    binaryExpression.getLeft(),
                    oldConstraints,
                    leftConstraints,
                    complement
                );
                final Satisfiability rightCondition = Normalizer.normalizeCondition(
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
                            Utils.mergeConstraints(newConstraints, rightConstraints);
                            return Satisfiability.UNKNOWN;
                        }
                    }
                    if (leftCondition == Satisfiability.NOT_SATISFIED) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    if (rightCondition == Satisfiability.NOT_SATISFIED) {
                        return Satisfiability.NOT_SATISFIED;
                    }
                    Utils.mergeConstraints(newConstraints, leftConstraints);
                    Utils.mergeConstraints(newConstraints, rightConstraints);
                    return Satisfiability.UNKNOWN;
                }
                //if ((operator == BinaryOperator.LOGICAL_OR && !complement) || (operator == BinaryOperator.LOGICAL_AND && complement)) {
                if (leftCondition == Satisfiability.SATISFIED) {
                    return Satisfiability.SATISFIED;
                }
                if (leftCondition == Satisfiability.NOT_SATISFIED) {
                    Utils.mergeConstraints(newConstraints, rightConstraints);
                    return rightCondition;
                }
                if (rightCondition == Satisfiability.SATISFIED) {
                    return Satisfiability.SATISFIED;
                }
                if (rightCondition == Satisfiability.NOT_SATISFIED) {
                    Utils.mergeConstraints(newConstraints, leftConstraints);
                }
                return Satisfiability.UNKNOWN;
                // }
            }
            return Normalizer.normalizeExpression(
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
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints,
        final boolean complement
    ) {
        final Polynomial leftPolynomial = Simplifier.simplify(expression.getLeft(), 3);
        final Polynomial rightPolynomial = Simplifier.simplify(expression.getRight(), 3);
        final Polynomial one = new Polynomial(3, 1);
        if (leftPolynomial.isValid() && rightPolynomial.isValid()) {
            final BinaryOperator operator = expression.getOperator();
            if (
                ((operator == BinaryOperator.COMPARISON_EQ) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_NE) && complement)
            ) {
                final Polynomial first = new Polynomial(leftPolynomial);
                first.subtract(rightPolynomial);
                first.add(one);
                final Polynomial second = new Polynomial(rightPolynomial);
                second.subtract(leftPolynomial);
                second.add(one);

                final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> firstMap = new HashMap<>();
                final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> secondMap = new HashMap<>();

                final Satisfiability result1 = Normalizer.normalizePolynomial(
                    first,
                    oldConstraints,
                    firstMap
                );
                final Satisfiability result2 = Normalizer.normalizePolynomial(
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
                        Utils.mergeConstraints(newConstraints, firstMap);
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
                    Utils.mergeConstraints(newConstraints, firstMap);
                    Utils.mergeConstraints(newConstraints, secondMap);
                    return Satisfiability.UNKNOWN;
                }
            }
            if (
                ((operator == BinaryOperator.COMPARISON_NE) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_EQ) && complement)
            ) {
                final Polynomial first = new Polynomial(leftPolynomial);
                first.subtract(rightPolynomial);
                final Polynomial second = new Polynomial(rightPolynomial);
                second.subtract(leftPolynomial);

                final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> firstMap = new HashMap<>();
                final Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> secondMap = new HashMap<>();

                final Satisfiability result1 = Normalizer.normalizePolynomial(
                    first,
                    oldConstraints,
                    firstMap
                );
                final Satisfiability result2 = Normalizer.normalizePolynomial(
                    second,
                    oldConstraints,
                    secondMap
                );

                if (result1 == Satisfiability.SATISFIED) {
                    return Satisfiability.SATISFIED;
                }
                if (result1 == Satisfiability.NOT_SATISFIED) {
                    Utils.mergeConstraints(newConstraints, secondMap);
                    return result2;
                }
                if (result1 == Satisfiability.UNKNOWN) {
                    if (result2 == Satisfiability.SATISFIED) {
                        return Satisfiability.SATISFIED;
                    }
                    if (result2 == Satisfiability.NOT_SATISFIED) {
                        Utils.mergeConstraints(newConstraints, firstMap);
                    }
                    return result2;
                }
            }
            Polynomial polynomialToCheck = null;
            if (
                ((operator == BinaryOperator.COMPARISON_LT) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_GE) && complement)
            ) {
                // !(i >= arr_len)
                // i < arr_len --> arr_len > i --> arr_len - i > 0

                // a < b ---> b > a ---> b - a > 0
                polynomialToCheck = new Polynomial(rightPolynomial);
                polynomialToCheck.subtract(leftPolynomial);
            }
            if (
                ((operator == BinaryOperator.COMPARISON_GE) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_LT) && complement)
            ) {
                // a >= b --> a-b >= 0 --> a-b > -1 --> a - b + 1 > 0

                polynomialToCheck = new Polynomial(leftPolynomial);
                polynomialToCheck.subtract(rightPolynomial);
                polynomialToCheck.add(one);
            }
            if (
                ((operator == BinaryOperator.COMPARISON_LE) && !complement) ||
                ((operator == BinaryOperator.COMPARISON_GT) && complement)
            ) {
                // a <= b --> b >= a ---> b - a >= 0 --> b - a > -1 --> b-a+1 > 0
                polynomialToCheck = new Polynomial(rightPolynomial);
                polynomialToCheck.subtract(leftPolynomial);
                polynomialToCheck.add(one);
            }
            if (
                ((operator == BinaryOperator.COMPARISON_GT) && !complement) ||
                ((expression.getOperator() == BinaryOperator.COMPARISON_LE) && complement)
            ) {
                // a > b --> a - b > 0
                polynomialToCheck = new Polynomial(leftPolynomial);
                polynomialToCheck.subtract(rightPolynomial);
            }
            if (polynomialToCheck != null) {
                return Normalizer.normalizePolynomial(
                    polynomialToCheck,
                    oldConstraints,
                    newConstraints
                );
            }
        }
        return Satisfiability.UNKNOWN;
    }

    private static Satisfiability normalizePolynomial(
        final Polynomial polynomialToNormalize,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints
    ) {
        if (polynomialToNormalize.isValid() && (polynomialToNormalize.getSize() >= 2)) {
            final Monomial a = polynomialToNormalize.getMonomial(0);
            final Monomial b = polynomialToNormalize.getMonomial(1);
            @Nullable
            final Monomial c = (polynomialToNormalize.getSize() == 2)
                ? null
                : polynomialToNormalize.getMonomial(2);
            final int constant = polynomialToNormalize.getConstant();

            if ((a != null) && (b != null)) {
                if (c == null) {
                    if (a.getCoefficient() == 1) {
                        final Satisfiability result = Normalizer.checkExpression(
                            a.getVariable(),
                            b.getVariable(),
                            null,
                            -b.getCoefficient(),
                            -constant,
                            oldConstraints,
                            newConstraints
                        );
                        if ((result != Satisfiability.SATISFIED) && (b.getCoefficient() == 1)) {
                            final Satisfiability result2 = Normalizer.checkExpression(
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
                        return Normalizer.checkExpression(
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
                        final Satisfiability result1 = Normalizer.checkExpression(
                            a.getVariable(),
                            b.getVariable(),
                            c.getVariable(),
                            -b.getCoefficient(),
                            -constant,
                            oldConstraints,
                            newConstraints
                        );
                        if ((result1 != Satisfiability.SATISFIED) && (b.getCoefficient() == 1)) {
                            final Satisfiability result2 = Normalizer.checkExpression(
                                b.getVariable(),
                                a.getVariable(),
                                c.getVariable(),
                                -a.getCoefficient(),
                                -constant,
                                oldConstraints,
                                newConstraints
                            );
                            if (result2 != Satisfiability.SATISFIED) {
                                final Satisfiability result3 = Normalizer.checkExpression(
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
                    return Normalizer.checkExpression(
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
                    return Normalizer.checkExpression(
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
        final @NotNull Map<@NotNull Variable, @NotNull Set<@NotNull Constraint>> oldConstraints,
        final @Nullable Map<? super @NotNull Variable, @NotNull Set<@NotNull Constraint>> newConstraints
    ) {
        final boolean normalDirectionSatified = Normalizer.checkVariableConstraint(
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
            final boolean inverseDirectionSatisfied = Normalizer.checkVariableConstraint(
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
            Utils.addConstraint(newConstraints, x, new Constraint(y, z, k1, k2));
        }

        return Satisfiability.UNKNOWN;
    }

    private static boolean checkVariableConstraint(
        @NotNull final Variable x,
        @NotNull final Variable y,
        @Nullable final Variable z,
        final int k1,
        final int k2,
        final @NotNull Map<@NotNull Variable, ? extends @NotNull Set<@NotNull Constraint>> oldConstraints
    ) {
        // arr_len - 1*(i) > 0
        // {i=[(array_length, ⊥, 1, -2)], array_length=[(i, ⊥, 1, 0)]}

        // arr_len(i, _, 1, >=0)

        // i >= arr_len -----> i - arr_len >= 0 ---> i-arr_len > -1
        // i - 1*arr_len > 1

        @Nullable
        final Set<@NotNull Constraint> constraints = oldConstraints.get(x);
        if (constraints != null) {
            for (@NotNull final Constraint constraint : constraints) {
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
