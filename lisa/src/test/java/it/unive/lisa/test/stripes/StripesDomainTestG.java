package it.unive.lisa.test.stripes;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.test.stripes.cfg.program.EndedProgram;
import it.unive.lisa.test.stripes.cfg.program.For;
import it.unive.lisa.test.stripes.cfg.program.If;
import it.unive.lisa.test.stripes.cfg.program.Program;
import it.unive.lisa.test.stripes.cfg.program.While;
import java.io.IOException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-22
 * @since version date
 */
@SuppressWarnings({ "DuplicateStringLiteralInspection", "StandardVariableNames" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StripesDomainTestG {

    @Test
    public void test01() throws IOException, ParsingException, AnalysisException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = new int[5]", StripesVariable.TOP)
            .next("b = new int[3][3]", StripesVariable.TOP)
            .next("c = b[1][1]", StripesVariable.TOP)
            .returnProgram("return", StripesVariable.TOP);
        StripesDomainTest.checkProgram(
            p,
            "test1.imp",
            "analysis___untyped_test1.test1(test1_this).dot"
        );
    }

    @Test
    public void test02() throws IOException, ParsingException, AnalysisException {
        final StripesVariable z = new StripesVariable("z");
        final StripesVariable y = new StripesVariable("y");
        final StripesVariable x = new StripesVariable("x");
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("z = 9", StripesVariable.TOP)
            .next("y = 0", StripesVariable.TOP)
            .next("x = y", x.add(y, null, 1, -1), y.add(x, null, 1, -1))
            .next("x = -3", StripesVariable.TOP)
            .next("a = y", a.clearAndAdd(y, null, 1, -1), y.clearAndAdd(a, null, 1, -1))
            .next("b = x", a, y, b.clearAndAdd(x, null, 1, -1), x.clearAndAdd(b, null, 1, -1))
            .next(
                "x = y",
                a.add(x, null, 1, -1),
                y.add(x, null, 1, -1),
                x.remove(b).add(y, null, 1, -1).add(a, null, 1, -1)
            )
            .next(
                "x = -y",
                a.remove(x).add(x, null, -1, -1),
                x.clearAndAdd(y, null, -1, -1).add(a, null, -1, -1),
                y.remove(x).add(x, null, -1, -1) // y = -x
            )
            .next(
                "x = y + z",
                a.remove(x),
                y.remove(x),
                x.clearAndAdd(y, z, 1, -1).add(a, z, 1, -1)
            )
            .next("x = 2*y", a.remove(x), y, x.clearAndAdd(y, null, 2, -1).add(a, null, 2, -1))
            .next("x = 2*y+2*z", a.remove(x), y, x.clearAndAdd(y, z, 2, -1).add(a, z, 2, -1))
            .next("x = y+y", a.remove(x), y, x.clearAndAdd(y, null, 2, -1).add(a, null, 2, -1))
            .next("x = 3*y - y", a.remove(x), y, x.clearAndAdd(y, null, 2, -1).add(a, null, 2, -1))
            .next(
                "x = y + 2*y + 3*z + 9 + y + 5+ z -y - 2*z + 7 + z",
                a.remove(x),
                y,
                x.clearAndAdd(y, z, 3, 20).add(a, z, 3, 20)
            )
            .next("x = 3*y + 3*z + 21", a.remove(x), y, x.clearAndAdd(y, z, 3, 20).add(a, z, 3, 20))
            .next("x = 2*y + 2*z + 3", a.remove(x), y, x.clearAndAdd(y, z, 2, 2).add(a, z, 2, 2))
            .next("x = 2 * (y+z) + 3", a.remove(x), y, x.clearAndAdd(y, z, 2, 2).add(a, z, 2, 2))
            .next("x = 2 * (y+z) - 5", a.remove(x), y, x.clearAndAdd(y, z, 2, -6).add(a, z, 2, -6))
            .next("x = 2*x + 2* y", a.remove(x), y)
            .next("x = 2*(x+y)", a.remove(x), y)
            .returnProgram("return", a, y);
        StripesDomainTest.checkProgram(
            p,
            "test2.imp",
            "analysis___untyped_test1.test2(test1_this).dot"
        );
    }

    @Test
    public void test03() throws IOException, ParsingException, AnalysisException {
        final StripesVariable y = new StripesVariable("y");
        final StripesVariable x = new StripesVariable("x");
        final StripesVariable a = new StripesVariable("a");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("y = 0", StripesVariable.TOP)
            .next("x = y", x.add(y, null, 1, -1), y.add(x, null, 1, -1))
            .next(
                "a = y",
                a.add(y, null, 1, -1).add(x, null, 1, -1),
                y.add(a, null, 1, -1),
                x.add(a, null, 1, -1)
            )
            .next("x = 2", a.remove(x), y.remove(x))
            .returnProgram("return 4", a, y);
        StripesDomainTest.checkProgram(
            p,
            "test3.imp",
            "analysis___untyped_test1.test3(test1_this).dot"
        );
    }

    @Test
    public void test04() throws IOException, ParsingException, AnalysisException {
        final StripesVariable x = new StripesVariable("x");
        final StripesVariable y = new StripesVariable("y");
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("x = 1", StripesVariable.TOP)
            .next("y = 2", StripesVariable.TOP)
            .next("a = x + y + 3 + 5 * (x + 2 * (x + y))", StripesVariable.TOP)
            .next("b = x + y + 3 + 5*x + 10*x + 10*y", StripesVariable.TOP)
            .next("c = 16 * x + 11 * y + 3", StripesVariable.TOP)
            .next("a = x + y + 3 + 5 * (2 * (x + y))", a.add(x, y, 11, 2))
            .next(
                "b = x + y + 3 + 10*x + 10*y",
                a.add(b, null, 1, -1),
                b.add(x, y, 11, 2) //NO: .add(a, null, 1, -1)
            )
            .next(
                "c = 11 * x + 11 * y + 3",
                a.add(c, null, 1, -1),
                b.add(c, null, 1, -1),
                c.add(x, y, 11, 2) //NO: .add(a, null, 1, -1).add(b, null, 1, -1)
            )
            .returnProgram("return", a, b, c);
        StripesDomainTest.checkProgram(
            p,
            "test4.imp",
            "analysis___untyped_test1.test4(test1_this).dot"
        );
    }

    @Test
    public void test05() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable g = new StripesVariable("g");
        final StripesVariable k = new StripesVariable("k");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("c = 10", StripesVariable.TOP)
            .next("d = 15", StripesVariable.TOP)
            .next("f = 2*(c + d)", f.add(c, d, 2, -1))
            .next("g = f * 0", f)
            .next("a = \"ciao\"", f)
            .next("b = strcat(a,\"ccc\")", f)
            .next("e = new int[2]", f)
            .next("e[0] = 1", f)
            .next("e[1] = 2", f)
            .next("k = e[0] + e[1] + 3", f)
            .returnProgram("return", f);
        StripesDomainTest.checkProgram(
            p,
            "test5.imp",
            "analysis___untyped_test1.test5(test1_this).dot"
        );
    }

    @Test
    public void test06() throws IOException, ParsingException, AnalysisException {
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable i = new StripesVariable("i");
        final StripesVariable k = new StripesVariable("k");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("c = 10", StripesVariable.TOP)
            .next("d = 15", StripesVariable.TOP)
            .next("e = new int[2]", StripesVariable.TOP)
            .next("f = c+d", f.add(c, d, 1, -1))
            .next("k = e[0] + e[1] + 3", f)
            .next("i = 0", f)
            .next("e[i] = 0", f)
            .returnProgram("return", f);
        StripesDomainTest.checkProgram(
            p,
            "test6.imp",
            "analysis___untyped_test1.test6(test1_this).dot"
        );
    }

    @Test
    public void test07() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable g = new StripesVariable("g");
        final StripesVariable h = new StripesVariable("h");
        final StripesVariable i = new StripesVariable("i");
        final StripesVariable j = new StripesVariable("j");
        final StripesVariable k = new StripesVariable("k");
        final StripesVariable l = new StripesVariable("l");
        final StripesVariable m = new StripesVariable("m");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = \"ciao\"", StripesVariable.TOP)
            .next("b = 10 + strlen(a)", StripesVariable.TOP)
            .next("c = b + 1", c.add(b, null, 1, 0), b.add(c, null, 1, -2))
            .next("d = true", c, b)
            .next("e = false", c, b)
            .next("f = d && e", c, b)
            .next("g = !d || e", c, b)
            .next("h = strrep(a, \"ciao\", \"ahah\")", c, b)
            .next("i = strsub(h, 1,3)", c, b)
            .next("j = (d || !e) && ((true && true) || (e || !d))", c, b)
            .next("k = !d", c, b)
            .next("m = 2*b + 3", c, b, m.add(b, null, 2, 2).add(c, null, 2, 0))
            .next("l = new float[10][c]", c, b, m)
            .returnProgram("return", c, b, m);
        StripesDomainTest.checkProgram(
            p,
            "test7.imp",
            "analysis___untyped_test1.test7(test1_this).dot"
        );
    }

    @Test
    public void test08() throws IOException, ParsingException, AnalysisException {
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable i = new StripesVariable("i");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("c = 1", StripesVariable.TOP)
            .next("d = 4", StripesVariable.TOP)
            .next("e = c + d", e.clearAndAdd(c, d, 1, -1))
            .next(
                new If<>("e > 6", e)
                    .thenBlock()
                    .next("e = 10", StripesVariable.TOP)
                    .next("f = 1", StripesVariable.TOP)
                    .elseBlock()
                    .next("e = 2*(d+c)", e.clearAndAdd(d, c, 2, -1))
                    .next("f = 3", e)
            )
            .next(
                new If<>("e > 6", StripesVariable.TOP)
                    .thenBlock()
                    .next("e = 5*(d + c)", e.clearAndAdd(d, c, 5, -1))
                    .next("f = 1", e)
                    .elseBlock()
                    .next("e = 2*(d+c)", e.clearAndAdd(d, c, 2, -1))
                    .next("f = 3", e)
            )
            .next(
                new If<>("e > 6", StripesVariable.TOP)
                    .thenBlock()
                    .next("e = 2*(d + c)", e.clearAndAdd(d, c, 2, -1))
                    .next("f = 1", e)
                    .elseBlock()
                    .next("e = 2*(d+c)", e.clearAndAdd(d, c, 2, -1))
                    .next("f = 3", e)
            )
            .next(
                new If<>("e > 6", e)
                    .thenBlock()
                    .next("e = 2*(d + c) + 5", e.clearAndAdd(d, c, 2, 4))
                    .next("f = 1", e)
                    .elseBlock()
                    .next("e = 2*(d + c) + 3", e.clearAndAdd(d, c, 2, 2))
                    .next("f = 3", e)
            )
            .returnProgram("return", e.clearAndAdd(d, c, 2, 2));
        StripesDomainTest.checkProgram(
            p,
            "test8.imp",
            "analysis___untyped_test1.test8(test1_this).dot"
        );
    }

    @Test
    public void test09() throws IOException, ParsingException, AnalysisException {
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("c = 1", StripesVariable.TOP)
            .next("d = 4", StripesVariable.TOP)
            .next("e = c + d", e.clearAndAdd(c, d, 1, -1))
            .next("f = (-d) - e", e, f.clearAndAdd(d, e, -1, -1))
            .next(
                new While<>("e > 0", f.clearAndAdd(d, e, -1, -1))
                    .next("e = e - 1", StripesVariable.TOP)
                    .next("f = (-d) - e", f.clearAndAdd(d, e, -1, -1))
            )
            .returnProgram("return", f.clearAndAdd(d, e, -1, -1));
        StripesDomainTest.checkProgram(
            p,
            "test9.imp",
            "analysis___untyped_test1.test9(test1_this).dot"
        );
    }

    @Test
    public void test10() throws IOException, ParsingException, AnalysisException {
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("c = 1", StripesVariable.TOP)
            .next("d = 4", StripesVariable.TOP)
            .next("e = c + d", e.clearAndAdd(c, d, 1, -1))
            .next("f = (-d) - e (FIRST)", e, f.clearAndAdd(d, e, -1, -1))
            .next(
                new While<>("c > 0", f)
                    .next("d = e - 1", d.clearAndAdd(e, null, 1, -2), e.clearAndAdd(d, null, 1, 0))
                    .next(
                        "f = (-d) - e (SECOND)",
                        d,
                        e,
                        f.clearAndAdd(d, e, -1, -1).add(e, null, -2, 0).add(d, null, -2, -2)
                    )
                // f = -d -e --> f = -(e-1)-e --> -e+1-e --> f = -2e+1
            )
            .returnProgram("return", f.clearAndAdd(d, e, -1, -1));
        StripesDomainTest.checkProgram(
            p,
            "test10.imp",
            "analysis___untyped_test1.test10(test1_this).dot"
        );
    }

    @Test
    public void test11() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable i = new StripesVariable("i");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 1", StripesVariable.TOP)
            .next("b = 2", StripesVariable.TOP)
            .next("c = 3", StripesVariable.TOP)
            .next("d = 10", StripesVariable.TOP)
            .next(
                new For<StripesVariable>()
                    .initialization("i = 0", StripesVariable.TOP)
                    .condition("i < c", StripesVariable.TOP)
                    .next(
                        new While<>("a + b > 0", c.clearAndAdd(i, null, 1, 0))
                            .next(
                                new If<>(
                                    "d > 10",
                                    c
                                    /*, a.add(b,null, -1, 0), b.add
                                                                (a, null, -1, 0)*/
                                )
                                    .thenBlock()
                                    .next("d = d - 1", c)
                                    .next(
                                        "b = d",
                                        c,
                                        b.clearAndAdd(d, null, 1, -1),
                                        d.clearAndAdd(b, null, 1, -1)
                                    )
                                    .next(
                                        "e = a + b (FIRST)",
                                        c,
                                        b,
                                        d,
                                        e.clearAndAdd(a, b, 1, -1).add(a, d, 1, -1)
                                    )
                                    .elseBlock()
                                    .next("f = 5", c)
                                    .next("e = a + b (SECOND)", c, e.clearAndAdd(a, b, 1, -1))
                            )
                            .next("g = 3", c)
                    )
                    .increment("i = i + 1", StripesVariable.TOP)
            )
            // i - c > -1
            .returnProgram("return", i.clearAndAdd(c, null, 1, -1));
        StripesDomainTest.checkProgram(
            p,
            "test11.imp",
            "analysis___untyped_test1.test11(test1_this).dot"
        );
    }

    @Test
    public void test12() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable g = new StripesVariable("g");
        final StripesVariable h = new StripesVariable("h");
        final StripesVariable i = new StripesVariable("i");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 1", StripesVariable.TOP)
            .next("b = 2", StripesVariable.TOP)
            .next("c = 3", StripesVariable.TOP)
            .next("d = a + b", d.clearAndAdd(a, b, 1, -1))
            .next("e = b + c", d, e.clearAndAdd(b, c, 1, -1))
            .next("f = a + c", d, e, f.clearAndAdd(a, c, 1, -1))
            .next("i = 0", d, e, f)
            .next(
                new For<StripesVariable>()
                    .initialization(
                        "i = d",
                        i.clearAndAdd(d, null, 1, -1).add(a, b, 1, -1),
                        d.clearAndAdd(a, b, 1, -1).add(i, null, 1, -1),
                        e,
                        f
                    )
                    .condition("i < e", d.remove(i), e)
                    .next("g = d + f", d, e.add(i, null, 1, 0), g.add(d, f, 1, -1))
                    .next(
                        new If<>("g >= 1", d, e, g)
                            .thenBlock()
                            .next(
                                "g = d + 1", // d = a+b --> g = d + 1 -->
                                // g = a+b+1 g > a+b
                                d.clearAndAdd(a, b, 1, -1).add(g, null, 1, -2),
                                e.clearAndAdd(b, c, 1, -1).add(i, null, 1, 0),
                                g.clearAndAdd(d, null, 1, 0).add(a, b, 1, 0)
                            )
                            .next(
                                new If<>("i == 0", d, e, g)
                                    .thenBlock()
                                    .next("f = 0", d, e, g)
                                    .next("g = d + 1", d, e, g)
                            )
                            .next("h = 9", d, e, g)
                            .elseBlock()
                            .next(
                                "g = d + 3",
                                d.clearAndAdd(a, b, 1, -1).add(g, null, 1, -4),
                                e.clearAndAdd(b, c, 1, -1).add(i, null, 1, 0),
                                g.clearAndAdd(d, null, 1, 2).add(a, b, 1, 2)
                            )
                            .next(
                                new If<>("a != 0", d, e, g)
                                    .thenBlock()
                                    .next(
                                        "f = a + b + 1",
                                        d.add(f, null, 1, -2),
                                        e,
                                        f.clearAndAdd(a, b, 1, 0),
                                        g.add(f, null, 1, 1)
                                    )
                                    .elseBlock()
                                    .next(
                                        "f = a + b + 10",
                                        d
                                            .clearAndAdd(a, b, 1, -1)
                                            .add(g, null, 1, -4)
                                            .add(f, null, 1, -11),
                                        e,
                                        f.clearAndAdd(a, b, 1, 9),
                                        g
                                            .clearAndAdd(d, null, 1, 2)
                                            .add(a, b, 1, 2)
                                            .add(f, null, 1, -8)
                                    )
                                    .next("g = (3*d + 6)/3 + 1", d, e, f, g)
                            )
                    )
                    .increment(
                        "i = i + 1",
                        d.clearAndAdd(a, b, 1, -1).add(g, null, 1, -4),
                        e.clearAndAdd(b, c, 1, -1),
                        g.clearAndAdd(d, null, 1, 0).add(a, b, 1, 0)
                    )
            )
            .returnProgram(
                "return",
                d.clearAndAdd(a, b, 1, -1),
                e.clearAndAdd(b, c, 1, -1),
                i.clearAndAdd(e, null, 1, -1).add(b, c, 1, -2) //TODO:?.add(b, c, 1, -1)
            );
        StripesDomainTest.checkProgram(
            p,
            "test12.imp",
            "analysis___untyped_test1.test12(test1_this).dot"
        );
    }

    @Test
    public void test13() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = 5", StripesVariable.TOP)
            .next("c = 1", StripesVariable.TOP)
            .next("a = -2*(b + c)", a.clearAndAdd(b, c, -2, -1))
            .next(
                new If<>("a - 2*(b+c) > 0", a)
                    .thenBlock()
                    .next("d = a + b", a.add(b, c, 2, 0), d.clearAndAdd(a, b, 1, -1))
            )
            // d = a + b ---> d > 2*(b+c) + b

            .returnProgram("return", a.clearAndAdd(b, c, -2, -1));
        StripesDomainTest.checkProgram(
            p,
            "test13.imp",
            "analysis___untyped_test1.test13(test1_this).dot"
        );
    }

    @Test
    public void test14() throws IOException, ParsingException, AnalysisException {
        final StripesVariable x = new StripesVariable("x");
        final StripesVariable y = new StripesVariable("y");
        final StripesVariable z = new StripesVariable("z");
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("x = 10", StripesVariable.TOP)
            .next("y = 1", StripesVariable.TOP)
            .next("z = 2", StripesVariable.TOP)
            .next("y = 2*z", y.add(z, null, 2, -1))
            .next("x = y", x.add(y, null, 1, -1).add(z, null, 2, -1), y.add(x, null, 1, -1))
            .next("a = 20", x, y)
            .next("b = 2", x, y)
            .next("c = 4", x, y)
            .next("a = 3*b", x, y, a.add(b, null, 3, -1))
            .next(
                "c = b",
                x,
                y,
                a.add(c, null, 3, -1),
                c.add(b, null, 1, -1),
                b.add(c, null, 1, -1)
            )
            .returnProgram("return", x, y, a, b, c);
        StripesDomainTest.checkProgram(
            p,
            "test14.imp",
            "analysis___untyped_test1.test14(test1_this).dot"
        );
    }

    @Test
    public void test15() throws IOException, ParsingException, AnalysisException {
        final StripesVariable x = new StripesVariable("x");
        final StripesVariable v1 = new StripesVariable("v1");
        final StripesVariable u = new StripesVariable("u");
        final StripesVariable v = new StripesVariable("v");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("x = 10", StripesVariable.TOP)
            .next("v1 = 5", StripesVariable.TOP)
            .next("u = 1", StripesVariable.TOP)
            .next("v = 2", StripesVariable.TOP)
            .next("x = 0", StripesVariable.TOP)
            .next("v1 = 2*x", v1.add(x, null, 2, -1))
            .next("x = u+v", v1.clearAndAdd(u, v, 2, -1), x.add(u, v, 1, -1))
            .returnProgram("return", v1, x);
        StripesDomainTest.checkProgram(
            p,
            "test15.imp",
            "analysis___untyped_test1.test15(test1_this).dot"
        );
    }

    @Test
    public void test16() throws IOException, ParsingException, AnalysisException {
        final StripesVariable x = new StripesVariable("x");
        final StripesVariable u = new StripesVariable("u");
        final StripesVariable v = new StripesVariable("v");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("x = 10", StripesVariable.TOP)
            .next("u = 5", StripesVariable.TOP)
            .next("v = 1", StripesVariable.TOP)
            .next("x = u - v", u.add(x, v, 1, -1))
            .returnProgram("return", u);
        StripesDomainTest.checkProgram(
            p,
            "test16.imp",
            "analysis___untyped_test1.test16(test1_this).dot"
        );
    }

    @Test
    public void test17() throws IOException, ParsingException, AnalysisException {
        final StripesVariable arrayLen = new StripesVariable("array_length");
        final StripesVariable i = new StripesVariable("i");
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable ahahahah = new StripesVariable("ahahahah");
        final StripesVariable b = new StripesVariable("b");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("array_length = 10", StripesVariable.TOP)
            .next(
                "i = array_length - 1",
                i.clearAndAdd(arrayLen, null, 1, -2),
                arrayLen.add(i, null, 1, 0)
            )
            .next(
                new If<>("i >= array_length", i, arrayLen)
                    .thenBlock()
                    .next("a = 7", StripesVariable.BOTTOM)
            )
            .next(
                new If<>("i <= array_length + 100", i.clearAndAdd(arrayLen, null, 1, -2), arrayLen)
                    .thenBlock()
                    .next("c = 9", i, arrayLen)
                    .elseBlock()
                    .next("ahahahah = 10", StripesVariable.BOTTOM)
            )
            .next("a = 5", i, arrayLen)
            .next("b = 9", i, arrayLen)
            .next(
                new If<>("a > b", i, arrayLen)
                    .thenBlock()
                    .next("c = 17", i, arrayLen, a.add(b, null, 1, 0))
            )
            .returnProgram("return", i, arrayLen);
        StripesDomainTest.checkProgram(
            p,
            "test17.imp",
            "analysis___untyped_test1.test17(test1_this).dot"
        );
    }

    @Test
    public void test18() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 100", StripesVariable.TOP)
            .next("b = 10", StripesVariable.TOP)
            .next(
                new If<>("a > b + 7", StripesVariable.TOP)
                    .thenBlock()
                    .next("c = 9", a.clearAndAdd(b, null, 1, 7))
                    .next(
                        new If<>("a > b + 10", a)
                            .thenBlock()
                            .next("d = 10", a.clearAndAdd(b, null, 1, 10))
                    )
                    .next(
                        new If<>("a > b + 4", a.clearAndAdd(b, null, 1, 7))
                            .thenBlock()
                            .next("d = 10", a)
                    )
            )
            .returnProgram("return", StripesVariable.TOP);
        StripesDomainTest.checkProgram(
            p,
            "test18.imp",
            "analysis___untyped_test1.test18(test1_this).dot"
        );
    }

    @Test
    public void test19() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable i = new StripesVariable("i");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 1", StripesVariable.TOP)
            .next("b = 9", StripesVariable.TOP)
            .next("i = 0", StripesVariable.TOP)
            .next(
                new For<StripesVariable>()
                    .condition("i < a", StripesVariable.TOP)
                    .next("b = 10", a.add(i, null, 1, 0))
                    .next("c = 10", a)
                    .increment("i = i + 1", StripesVariable.TOP)
            )
            .returnProgram("return", i.clearAndAdd(a, null, 1, -1));
        StripesDomainTest.checkProgram(
            p,
            "test19.imp",
            "analysis___untyped_test1.test19(test1_this).dot"
        );
    }

    @Test
    public void testImg1() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = a * 3 + 5/2 + a%3", StripesVariable.TOP)
            .next("c = a + 5*b - 7 - (6*a)/3 - 3*(b - a - 2) + 1", c.clearAndAdd(a, b, 2, 4))
            .next(
                new If<>("a > 3*b", c, a.clearAndAdd(b, null, 3, 0))
                    .thenBlock()
                    .next("d = 5*b", a, c, d.clearAndAdd(b, null, 5, -1))
                    .next(
                        new If<>("a > 3*b + 7", a, c, d)
                            .thenBlock()
                            .next("c = c + 1", a.clearAndAdd(b, null, 3, 7), d)
                    )
                    .next(
                        "b = 4*(a + d)",
                        a.clearAndAdd(b, null, 3, 0),
                        d,
                        b.clearAndAdd(a, d, 4, -1)
                    )
                    .next("c = 2*a+2*b", a, b, c.clearAndAdd(a, b, 2, -1), d)
            )
            .returnProgram("return c", c);
        StripesDomainTest.checkProgram(
            p,
            "test_img1.imp",
            "analysis___untyped_TestImg1.main(TestImg1_this).dot"
        );
    }

    @Test
    public void testImg2() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");
        final StripesVariable g = new StripesVariable("g");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 5", StripesVariable.TOP)
            .next("b = 3", StripesVariable.TOP)
            .next("c = 2*(a + b) + 1", c.clearAndAdd(a, b, 2, 0))
            // d - (-1) *c > 0
            // d = -c+1 --> -c = d - 1 --> c = -d+1 --> c - (-1)*d > 0

            // a = b --> b = a
            // a > b-1   b > a - 1

            // a = b+c ---> b = a-c
            //         ---> c = a-b ---> a > b+c-1

            // d = -(c) + 1
            // d = -c+1
            // d = -(2*(a+b)+1) + 1 --> -2a -2b-1+1 --> d = -2(ab) ---> NOOOO!!!
            // Because : // c > 2(a+b)   --> -c < -2(a+b)
            .next("d = -(c) + 1", c.add(d, null, -1, 0), d.clearAndAdd(c, null, -1, 0))
            // d = -c+1
            // 2*d = -2*c +2
            // 2*d+5 = -2*c+7
            // e = 2d+5
            .next("e = -2*(c) + 7", e.clearAndAdd(c, null, -2, 6).add(d, null, 2, 4), c, d)
                // e = -2*(c)+7
                // e > -2*c+6
                // 2*e = -4*(c) + 14
                
                // e >= -2c+7 --> e > -2c+6
                // e <= -2c+7 -2c + 7 >= e    -2c >= e-7   -2c > e-8
                
                //d = -c+1
                // 4*d = -4*c + 4
                // 4*d + 10 = -4*c+14
                // f = 4*d + 10 --> f > 4d+9
            .next(
                "f = -4*(c) + 14",
                f
                    .clearAndAdd(c, null, -4, 13)
                    //f = 2e : NO --> .add(e, null, 2, 0)
                    .add(d, null, 4, 9),
                c,
                d,
                    e
            )
                // g = a-b ---> b+g=a --> a = b+g
            .next("g = a - b", a.clearAndAdd(b, g, 1, -1), f, c, d, e)
            .returnProgram("return", a, c, d, e, f);

        StripesDomainTest.checkProgram(
            p,
            "test_img2.imp",
            "analysis___untyped_TestImg2.main(TestImg2_this).dot"
        );
    }

    @Test
    public void testImg3() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable d = new StripesVariable("d");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = 2*a + 7", b.clearAndAdd(a, null, 2, 6))
            .next(
                new If<>("b > a+a+4", b) // b > 2a+4 --> b <= 2a+4 --> 2a >= b+5
                    .thenBlock()
                    .next("a = 2*b", a.clearAndAdd(b, null, 2, -1))
                    .elseBlock()
                    .next("a = 5", StripesVariable.BOTTOM)
            )
            .next(
                "d = a + b",
                a.clearAndAdd(b, null, 2, -1),
                d.clearAndAdd(a, b, 1, -1).add(b, null, 3, -1)
            )
            .next(
                new While<>("(d < a+b-9 && a < 5*3) || (d >= a+b+5)", a, d)
                    .next("d = d*2", StripesVariable.BOTTOM)
            )
            .returnProgram("return d", a, d);
        StripesDomainTest.checkProgram(
            p,
            "test_img3.imp",
            "analysis___untyped_TestImg3.main(TestImg3_this).dot"
        );
    }
    /*
    @Test
    public void testImg1() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = 5", StripesVariable.TOP)
            .next("c = 1", StripesVariable.TOP)
            .next(
                new If<>("a > b", StripesVariable.TOP)
                    .thenBlock()
                    // a = b+c
                    .next("c = a - b", a.clearAndAdd(b, null, 1, 0).add(b, c, 1, -1))
                    .elseBlock()
                    // b > a -1
                    .next("c = 0", b.clearAndAdd(a, null, 1, -1))
            )
            .returnProgram("return c", StripesVariable.TOP);
        StripesDomainTest.checkProgram(
            p,
                "test_img1.imp",
            "analysis___untyped_TestImg1.main(TestImg1_this).dot"
        );
    }

    @Test
    public void testImg2() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");

        //noinspection ConstantConditions
        //Assert.assertThat("To implement!", false, CoreMatchers.is(true));

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 5", StripesVariable.TOP)
            .next("b = 3", StripesVariable.TOP)
            .next("c = 7", StripesVariable.TOP)
            .next(
                new While<>("c + b > 0", StripesVariable.TOP)
                    .next("d = a - b", a.add(d, b, 1, -1))
                    .next(
                        "e = d",
                        a.add(e, b, 1, -1),
                        e.clearAndAdd(d, null, 1, -1),
                        d.clearAndAdd(e, null, 1, -1)
                    )
                    .next("f = 2*b + 1", a, e, d, f.clearAndAdd(b, null, 2, 0))
                    // f = d

                    // (2)
                    // u = 9
                    // v = 14
                    // x = 2
                    // v1 = 2*x = 4          --> v1 -> [(x, bot, 2, -1)] ------> v1-2*x > -1
                    // x = u+v x = 23          --> x -> [(u, v, 1, -1)]    ------> x-1*(u+v) > -1
                    // --> v1 = u+v      --> v1 -> [(u,v,2, -1)]     ------> v1-2*(u+v) > -1

                    .next(
                        "d = f",
                        a,
                        e.remove(d).add(f, null, 1, -1),
                        d.clearAndAdd(f, null, 1, -1).add(b, null, 2, 0),
                        f.add(d, null, 1, -1)
                    )
                    .next("e = b")
                    .next("b = a + c")
            )
            .returnProgram("return");

        StripesDomainTest.checkProgram(
            p,
                "test_img2.imp",
            "analysis___untyped_TestImg2.main(TestImg2_this).dot"
        );
    }
    
    @Test
    public void testImg3_1() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = 5", StripesVariable.TOP)
            .next(
                new If<>("a > b", StripesVariable.TOP)
                    .thenBlock()
                    .next(
                        new While<>("b != a", a.add(b, null, 1, 0))
                            .next(
                                "c = b + 1",
                                a.add(c, null, 1, -1),
                                c.clearAndAdd(b, null, 1, 0),
                                b.add(c, null, 1, -2)
                            )
                    )
                // c = b + 1
                // a > b
                // a + 1 > b + 1
                // a + 1 > c
                // a - c > -1
            )
            // b > a -1
            .returnProgram("return", b.clearAndAdd(a, null, 1, -1));
        StripesDomainTest.checkProgram(
            p,
                "test_img3_1.imp",
            "analysis___untyped_TestImg3.main1(TestImg3_this).dot"
        );
    }

    @Test
    public void testImg3_2() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable i = new StripesVariable("i");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = 15", StripesVariable.TOP)
            .next("c = 5", StripesVariable.TOP)
            .next("d = a + b + 7", d.clearAndAdd(a, b, 1, 6))
            .next(
                new For<StripesVariable>()
                    .initialization("i = 0", d)
                    .condition("i < a", d)
                    .next(
                        new If<>("d > a + b + 2", d, a.clearAndAdd(i, null, 1, 0))
                            .thenBlock()
                            .next("c = 0", d, a)
                            .elseBlock()
                            .next("c = 1", StripesVariable.BOTTOM)
                    )
                    .increment("i = i + 1", d, a)
            )
            // i >= a
            .returnProgram("return", d, i.clearAndAdd(a, null, 1, -1));
        StripesDomainTest.checkProgram(
            p,
                "test_img3_2.imp",
            "analysis___untyped_TestImg3.main2(TestImg3_this).dot"
        );
    }

    @Test
    public void testImg3_3() throws IOException, ParsingException, AnalysisException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");

        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10", StripesVariable.TOP)
            .next("b = 15", StripesVariable.TOP)
            .next("d = a + b + 7", d.clearAndAdd(a, b, 1, 6))
            .next(
                "e = a",
                d.add(e, b, 1, 6),
                e.clearAndAdd(a, null, 1, -1),
                a.clearAndAdd(e, null, 1, -1)
            )
            .next(
                new If<>("(a < e) || (!(d <= a + b) && e < a)", d, e, a)
                    .thenBlock()
                    .next("c = 100", StripesVariable.BOTTOM)
                    .elseBlock()
                    .next("c = 50", d, e, a)
            )
            .returnProgram("return", d, e, a);
        StripesDomainTest.checkProgram(
            p,
                "test_img3_3.imp",
            "analysis___untyped_TestImg3.main3(TestImg3_this).dot"
        );
    }*/
}
