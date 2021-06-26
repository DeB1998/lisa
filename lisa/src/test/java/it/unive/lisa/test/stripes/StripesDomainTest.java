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
import it.unive.lisa.test.stripes.cfg.dot.DotReader;
import it.unive.lisa.test.stripes.cfg.dot.EdgeType;
import it.unive.lisa.test.stripes.cfg.graph.Cfg;
import it.unive.lisa.test.stripes.cfg.graph.InvalidCfgException;
import it.unive.lisa.test.stripes.cfg.program.EndedProgram;
import it.unive.lisa.test.stripes.cfg.program.Program;
import it.unive.lisa.test.stripes.cfg.program.While;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-17
 * @since version date
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class StripesDomainTest {
    
    private static final Pattern COMPILE = Pattern.compile(",\n");
    
    @Test
    public void test0() throws ParsingException, AnalysisException {
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
        lisa.run(IMPFrontend.processFile("imp-testcases/stripes/test1.imp"));
    }
    
    @Test
    public void test18() throws IOException {
        
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
                .next("a = 5")
                .next("b = 3")
                .next("c = 7")
                .next(
                        new While<StripesVariable>("c + b > 0")
                                .next("d = a - b")
                                .next("e = d")
                                .next("f = 2*b + 1")
                                .next("d = f")
                                .next("e = b")
                                .next("b = a + c")
                )
                .returnProgram("return");
        
        StripesDomainTest.checkProgram(p, "test2.dot");
    }
    
    public static void checkProgram(
        final EndedProgram<StripesVariable> program,
        final String dotFileName
    ) throws IOException {
        final Cfg<List<StripesVariable>> cfgProgram = program.getCfg(
            StripesDomainTest::programToCfgConverter
        );

        final DotReader<List<StripesVariable>> dotReader = new DotReader<>(
            "test-outputs/stripes/" + dotFileName
        );
        final Cfg<List<StripesVariable>> cfg = dotReader.readFile(
            StripesDomainTest::extractData,
            StripesDomainTest::identifierExtractor,
            StripesDomainTest::edgeTypeExtractor,
            StripesDomainTest::rootChecker
        );
        cfg.isEqualTo(
            cfgProgram,
            StripesDomainTest::equalityComputer,
            StripesDomainTest::nodesDiffersInSize,
            StripesDomainTest::nodesDiffersInData
        );
    }

    private static List<StripesVariable> programToCfgConverter(
        final String identifier,
        final List<StripesVariable> variables
    ) {
        return variables;
    }

    private static List<StripesVariable> extractData(final String label, final String color, final int peripheries) {
    
        //Pattern valuePattern = Pattern.compile("value \\[\\[ (?<value>[^]]*)", Pattern.DOTALL);
        final Pattern valuePattern = Pattern.compile(".*value \\[\\[ (?<value>[^]]*).*", Pattern.DOTALL);// \\[\\[");// (?<value>[^]]*) []][]]");
        String lab = label.trim();
        final Matcher m = valuePattern.matcher(lab);
        if (m.matches()) {
            String s = m.group("value").trim();
            final List<StripesVariable> result = new LinkedList<>();
            if (s.equals("⊤")) {
                result.add(StripesVariable.TOP);
                return result;
            }
            if (s.equals("⊥")) {
                result.add(StripesVariable.BOTTOM);
                return result;
            }
            s = s.replace("[", "");
            final String[] variables = StripesDomainTest.COMPILE.split(s);
            for (final String variable : variables) {
                final Pattern p2 = Pattern.compile("(?<name>\\w+) → [{](?<constr>[^}]*)}");
                final Matcher m2 = p2.matcher(variable);
                if (m2.matches()) {
                    final StripesVariable v = new StripesVariable(m2.group("name"));
                    final Pattern p3 = Pattern.compile("[)],");
                    final String[] constr = p3.split(m2.group("constr"));
                    for (final String c : constr) {
                        final String n = c.replace("(", "").replace(")", "").trim();
                        final Pattern p4 = Pattern.compile("(?<y>.*), (?<z>.*), (?<k1>.*), (?<k2>.*)");
                        final Matcher m3 = p4.matcher(n);
                        if (m3.matches()) {
                            v.add(new StripesVariable(m3.group("y")),
                                    m3.group("z").equals("⊥") ? null : new StripesVariable(m3.group("z")),
                                    Integer.parseInt(m3.group("k1")),
                                    Integer.parseInt(m3.group("k2")));
                        }
                    }
                    result.add(v);
                }
            }
            return result;
            //[a &rarr; {(d, b, 1, -1), (e, b, 1, -1)},<BR/>d &rarr; {(e, &perp;, 1, -1)},<BR/>e &rarr; {(d, &perp;, 1, -1)}]
            
            
            // d = -(a, b)<BR/>{{<BR/>heap [[ monolith ]]<BR/>value [[ [a &rarr; {(d, b, 1, -1)}] ]]<BR/>}} -&gt; [d]
        }
        return new LinkedList<>();
    }

    private static String identifierExtractor(
        final String label,
        final String color,
        final int peripheries
    ) {
        return label.split("\n")[0];
    }

    private static EdgeType edgeTypeExtractor(final String color, final String style) {
        if (style.equals("dashed")) {
            if (color.equals("blue")) {
                return EdgeType.TRUE;
            }
            if (color.equals("red")) {
                return EdgeType.FALSE;
            }
        }
        if (color.equals("black")) {
            return EdgeType.SEQUENCE;
        }
        throw new InvalidCfgException("Unknown edge with color " + color);
    }

    private static boolean rootChecker(
        final String label,
        final String color,
        final int peripheries
    ) {
        return color.equals("black") && (peripheries == 0);
    }

    private static boolean equalityComputer(
        final String firstIdentifier,
        final String secondIdentifier,
        final List<StripesVariable> firstData,
        final List<StripesVariable> secondData
    ) {
        for (StripesVariable firstVariable : firstData) {
            boolean found = false;
            for (StripesVariable secondVariable: secondData) {
                if (firstVariable.equals(secondVariable)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        /*boolean equal =
                    (firstIdentifier == null) ||
                    (secondIdentifier == null) ||
                    firstIdentifier.equals(secondIdentifier);*/

        return true; /*equal &&*/
    }

    private static void nodesDiffersInSize(
        final String firstIdentifier,
        final String secondIdentifier,
        final List<StripesVariable> firstData,
        final List<StripesVariable> secondData
    ) {
        throw new AssertionError(firstIdentifier + " differs in size from " + secondIdentifier);
    }

    private static void nodesDiffersInData(
        final String firstIdentifier,
        final String secondIdentifier,
        final List<StripesVariable> firstData,
        final List<StripesVariable> secondData
    ) {
        throw new AssertionError(
            String.format(
                "%s differs from %s in %s and %s",
                firstData,
                secondData,
                firstIdentifier,
                secondIdentifier
            )
        );
    }
}
