package it.unive.lisa.test.stripes;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.LiSAFactory;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.nonrelational.value.stripes.StripesDomain;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import org.junit.Test;
/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-17
 * @since version date
 */
public class StripesDomainTest {
    
    @Test
    public void testStripes() throws ParsingException, AnalysisException {
    
        
        LiSAConfiguration conf = new LiSAConfiguration();
        
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
        
        LiSA lisa = new LiSA(conf);
        lisa.run(IMPFrontend.processFile("imp-testcases/stripes/test1.imp"));
    }
}
