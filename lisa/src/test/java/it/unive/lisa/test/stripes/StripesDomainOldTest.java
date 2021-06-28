package it.unive.lisa.test.stripes;

import static org.hamcrest.CoreMatchers.is;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.LiSAFactory;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.nonrelational.value.stripes.Simplifier;
import it.unive.lisa.analysis.nonrelational.value.stripes.StripesDomain;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Monomial;
import it.unive.lisa.analysis.nonrelational.value.stripes.polinomial.Polynomial;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.lisa.test.stripes.simplifier.OldSimplifier;
import it.unive.lisa.test.stripes.simplifier.SimplificationResult;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-17
 * @since version date
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class StripesDomainOldTest {

    private static class TestDomain
        extends BaseLattice<TestDomain>
        implements ValueDomain<TestDomain> {

        @Override
        protected TestDomain lubAux(final TestDomain other) throws SemanticException {
            return this;
        }

        @Override
        protected TestDomain wideningAux(final TestDomain other) throws SemanticException {
            return this;
        }

        @Override
        protected boolean lessOrEqualAux(final TestDomain other) throws SemanticException {
            return false;
        }

        @Override
        public boolean equals(final Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public TestDomain top() {
            return this;
        }

        @Override
        public TestDomain bottom() {
            return this;
        }

        @SuppressWarnings("DuplicateStringLiteralInspection")
        @Override
        public TestDomain assign(
            final Identifier id,
            final ValueExpression expression,
            final ProgramPoint pp
        ) throws SemanticException {
            if ((pp instanceof Assignment) && (id instanceof Variable variable)) {
                System.out.println("asdsdfnnn");

                final SimplificationResult result1 = OldSimplifier.simplify(expression);
                final Polynomial result2 = Simplifier.simplify(expression, 2);

                if (result1 == null) {
                    Assert.assertThat("Result are different!", !result2.isValid(), is(true));
                } else {
                    final Variable firstVariable = result1.getFirstIdentifier();
                    final int firstIdentifierCount = result1.getFirstIdentifierCount();
                    final Variable secondVariable = result1.getSecondIdentifier();
                    final int secondIdentifierCount = result1.getSecondIdentifierCount();
                    final int constant = result1.getConstant();

                    Assert.assertThat(
                        "Constants are different in expression " + expression,
                        result2.getConstantCoefficient(),
                        is(constant)
                    );

                    if ((firstVariable == null) && (secondVariable == null)) {
                        Assert.assertThat(
                            "Result is not constant in expression " + expression,
                            result2.isConstantPolynomial(),
                            is(true)
                        );
                    } else if ((firstVariable != null) && (secondVariable == null)) {
                        Assert.assertThat(
                            "Monomials are different in expression " + expression,
                            result2.getSize(),
                            is((firstIdentifierCount == 0) ? 0 : 1)
                        );
                        if (firstIdentifierCount != 0) {
                            final Monomial monomial = result2.getMonomial(0);
                            Assert.assertThat(
                                "Monomials are different in expression " + expression,
                                monomial.getVariable(),
                                is(firstVariable)
                            );
                            Assert.assertThat(
                                "Monomials are different in expression " + expression,
                                monomial.getCoefficient(),
                                is(firstIdentifierCount)
                            );
                        }
                    } else {
                        Assert.assertThat(
                            "Monomials are different in expression " + expression,
                            result2.getSize(),
                            is(2)
                        );

                        final Monomial firstMonomial = result2.getMonomial(0);
                        final Monomial secondMonomial = result2.getMonomial(1);

                        if (firstMonomial.getVariable().equals(firstVariable)) {
                            Assert.assertThat(
                                "First Monomials are different in expression " + expression,
                                firstMonomial.getVariable(),
                                is(firstVariable)
                            );
                            Assert.assertThat(
                                "First Monomials are different in expression " + expression,
                                firstMonomial.getCoefficient(),
                                is(firstIdentifierCount)
                            );
                            Assert.assertThat(
                                "Second Monomials are different in expression " + expression,
                                secondMonomial.getVariable(),
                                is(secondVariable)
                            );
                            Assert.assertThat(
                                "Second Monomials are different in expression " + expression,
                                secondMonomial.getCoefficient(),
                                is(secondIdentifierCount)
                            );
                        } else {
                            Assert.assertThat(
                                "First Monomials are different in expression " + expression,
                                secondMonomial.getVariable(),
                                is(firstVariable)
                            );
                            Assert.assertThat(
                                "First Monomials are different in expression " + expression,
                                secondMonomial.getCoefficient(),
                                is(firstIdentifierCount)
                            );
                            Assert.assertThat(
                                "Second Monomials are different in expression " + expression,
                                firstMonomial.getVariable(),
                                is(secondVariable)
                            );
                            Assert.assertThat(
                                "Second Monomials are different in expression " + expression,
                                firstMonomial.getCoefficient(),
                                is(secondIdentifierCount)
                            );
                        }
                    }
                }
            }

            return this;
        }

        @Override
        public TestDomain smallStepSemantics(
            final ValueExpression expression,
            final ProgramPoint pp
        ) throws SemanticException {
            return this;
        }

        @Override
        public TestDomain assume(final ValueExpression expression, final ProgramPoint pp)
            throws SemanticException {
            return this;
        }

        @Override
        public TestDomain forgetIdentifier(final Identifier id) throws SemanticException {
            return this;
        }

        @Override
        public Satisfiability satisfies(final ValueExpression expression, final ProgramPoint pp)
            throws SemanticException {
            return Satisfiability.UNKNOWN;
        }

        @Override
        public String representation() {
            return "";
        }
    }

    @Test
    public void testStripes() throws ParsingException, AnalysisException {
        final LiSAConfiguration conf = new LiSAConfiguration();

        conf.setDumpAnalysis(true);
        conf.setWorkdir("test-outputs/stripes");
        conf.setAbstractState(
            LiSAFactory.getDefaultFor(
                AbstractState.class,
                LiSAFactory.getDefaultFor(HeapDomain.class),
                new StripesDomain()
            )
        );
        conf.setInferTypes(true);

        final LiSA lisa = new LiSA(conf);
        lisa.run(IMPFrontend.processFile("imp-testcases/stripes/test_img3.imp"));
    }

    @Test
    public void testSimplifiers() throws AnalysisException, ParsingException {
        final LiSAConfiguration conf = new LiSAConfiguration();

        conf.setDumpAnalysis(true);
        conf.setWorkdir("test-outputs/stripes");
        conf.setAbstractState(
            LiSAFactory.getDefaultFor(
                AbstractState.class,
                LiSAFactory.getDefaultFor(HeapDomain.class),
                new TestDomain()
            )
        );
        conf.setInferTypes(true);

        final LiSA lisa = new LiSA(conf);
        lisa.run(IMPFrontend.processFile("imp-testcases/stripes/test1.imp"));
    }
}
