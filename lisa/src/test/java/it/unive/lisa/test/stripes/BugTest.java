package it.unive.lisa.test.stripes;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.LiSAFactory;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import org.junit.Test;

import java.util.Objects;

public class BugTest {

    private static class FakeAnalysis extends BaseNonRelationalValueDomain<FakeAnalysis> {

        private static final FakeAnalysis TOP = new FakeAnalysis(100);
    
        private static final FakeAnalysis BOTTOM = new FakeAnalysis(0);
    
    
        private final int value;

        public FakeAnalysis(final int value) {
            this.value = value;
        }

        @Override
        public FakeAnalysis top() {
            return FakeAnalysis.TOP;
        }

        @Override
        public FakeAnalysis bottom() {
            return FakeAnalysis.BOTTOM;
        }

        @Override
        public String representation() {
            return "Value: " + this.value;
        }

        @Override
        protected FakeAnalysis lubAux(final FakeAnalysis other) {
            return this.value < other.value ? this : other;
        }

        @Override
        protected FakeAnalysis wideningAux(final FakeAnalysis other) {
            return this.value < other.value ? this : other;
        }

        @Override
        protected boolean lessOrEqualAux(final FakeAnalysis other) {
            return this.value < other.value;
        }
    
    
        @Override
        protected Satisfiability satisfiesBinaryExpression(final BinaryOperator operator
                , final FakeAnalysis left, final FakeAnalysis right, final ProgramPoint pp) {
        
            return Satisfiability.UNKNOWN;
        }
    
        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if ((o == null) || (this.getClass() != o.getClass())) return false;
            final FakeAnalysis that = (FakeAnalysis) o;
            return this.value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }
    }

    @Test
    public void test() throws ParsingException, AnalysisException {
        final LiSAConfiguration conf = new LiSAConfiguration();

        conf.setDumpAnalysis(true);
        conf.setDumpCFGs(true);
        conf.setWorkdir("test-outputs/stripes");
        conf.setAbstractState(
            LiSAFactory.getDefaultFor(
                AbstractState.class,
                LiSAFactory.getDefaultFor(HeapDomain.class),
                new FakeAnalysis(100)
            )
        );

        final LiSA lisa = new LiSA(conf);
        lisa.run(IMPFrontend.processFile("imp-testcases/stripes/testBug.imp"));
    }
}
