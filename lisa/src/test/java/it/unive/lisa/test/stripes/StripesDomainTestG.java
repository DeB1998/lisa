package it.unive.lisa.test.stripes;

import static org.hamcrest.CoreMatchers.*;

import it.unive.lisa.test.stripes.cfg.program.EndedProgram;
import it.unive.lisa.test.stripes.cfg.program.For;
import it.unive.lisa.test.stripes.cfg.program.If;
import it.unive.lisa.test.stripes.cfg.program.IfBlockWithElse;
import it.unive.lisa.test.stripes.cfg.program.Program;
import it.unive.lisa.test.stripes.cfg.program.While;
import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-22
 * @since version date
 */
@SuppressWarnings({ "DuplicateStringLiteralInspection", "StandardVariableNames" })
public class StripesDomainTestG {

    @Test
    public void test1() throws IOException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = new int[5]", StripesVariable.TOP)
            .next("b = new int[3][3]", StripesVariable.TOP)
            .next("c = b[1][1]", StripesVariable.TOP)
            .returnProgram("return", StripesVariable.TOP);
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test1(test1_this).dot");
    }

    @Test
    public void test2() throws IOException {
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
            .next("x = -y", a.remove(x), x.clearAndAdd(y, null, -1, -1), y.remove(x))
            .next("x = y + z", a, y, x.clearAndAdd(y, z, 1, -1))
            .next("x = 2*y", a, y, x.clearAndAdd(y, null, 2, -1))
            .next("x = 2*y+2*z", a, y, x.clearAndAdd(y, z, 2, -1))
            .next("x = y+y", a, y, x.clearAndAdd(y, null, 2, -1))
            .next("x = 3*y - y", a, y, x.clearAndAdd(y, null, 2, -1))
            .next(
                "x = y + 2*y + 3*z + 9 + y + 5+ z -y - 2*z + 7 + z",
                a,
                y,
                x.clearAndAdd(y, z, 3, 20)
            )
            .next("x = 3*y + 3*z + 21", a, y, x.clearAndAdd(y, z, 3, 20))
            .next("x = 2*y + 2*z + 3", a, y, x.clearAndAdd(y, z, 2, 2))
            .next("x = 2 * (y+z) + 3", a, y, x.clearAndAdd(y, z, 2, 2))
            .next("x = 2 * (y+z) - 5", a, y, x.clearAndAdd(y, z, 2, -6))
            .next("x = 2*x + 2* y", a, y)
            .next("x = 2*(x+y)", a, y)
            .returnProgram("return", a, y);
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test2(test1_this).dot");
    }

    @Test
    public void test3() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test3(test1_this).dot");
    }

    @Test
    public void test4() throws IOException {
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
            .next("b = x + y + 3 + 10*x + 10*y", a, b.add(x, y, 11, 2))
            .next("c = 11 * x + 11 * y + 3", a, b, c.add(x, y, 11, 2))
            .returnProgram("return", a, b, c);
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test4(test1_this).dot");
    }

    @Test
    public void test5() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test5(test1_this).dot");
    }

    @Test
    public void test6() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test6(test1_this).dot");
    }

    @Test
    public void test7() throws IOException {
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
            .next("m = 2*b + 3", c, b, m.add(b, null, 2, 2))
            .next("l = new float[10][c]", c, b, m)
            .returnProgram("return", c, b, m);
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test7(test1_this).dot");
    }

    @Test
    public void test8() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test8(test1_this).dot");
    }

    @Test
    public void test9() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test9(test1_this).dot");
    }

    @Test
    public void test10() throws IOException {
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
                new While<>("c > 0", f)
                    .next("d = e - 1", d.clearAndAdd(e, null, 1, -2), e.clearAndAdd(d, null, 1, 0))
                    .next("f = (-d) - e", d, e, f.clearAndAdd(d, e, -1, -1))
            )
            .returnProgram("return", f.clearAndAdd(d, e, -1, -1));
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test10(test1_this).dot");
    }

    @Test
    public void test11() throws IOException {
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
                                    /*, a.add(b,null, -1, 0), b.add(a, null, -1, 0)*/
                                )
                                    .thenBlock()
                                    .next("d = d - 1", c)
                                    .next(
                                        "b = d",
                                        c,
                                        b.clearAndAdd(d, null, 1, -1),
                                        d.clearAndAdd(b, null, 1, -1)
                                    )
                                    .next("e = a + b", c, b, d, e.clearAndAdd(a, b, 1, -1))
                                    .elseBlock()
                                    .next("f = 5", c)
                                    .next("e = a + b", c, e.clearAndAdd(a, b, 1, -1))
                            )
                            .next("g = 3", c)
                    )
                    .increment("i = i + 1", StripesVariable.TOP)
            )
            // i - c > -1
            .returnProgram("return", i.clearAndAdd(c, null, 1, -1));
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test11(test1_this).dot");
    }

    @Test
    public void test12() throws IOException {
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
                                "g = d + 1", // d = a+b --> g = d + 1 --> g = a+b+1 g > a+b
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
                                    .next("f = a + b + 1", d, e, f.clearAndAdd(a, b, 1, 0), g)
                                    .elseBlock()
                                    .next("f = a + b + 10", d, e, f.clearAndAdd(a, b, 1, 9), g)
                                    .next("g = (3*d + 6)/3 + 1", d, e, f, g)
                            )
                    )
                    .increment(
                        "i = i + 1",
                        d,
                        e.clearAndAdd(b, c, 1, -1),
                        g.clearAndAdd(d, null, 1, 0).add(a, b, 1, 0)
                    )
            )
            .returnProgram(
                "return",
                d.clearAndAdd(a, b, 1, -1),
                e.clearAndAdd(b, c, 1, -1)
            );
        StripesDomainTest.checkProgram(p, "test1.dot");
    }

    @Test
    public void test13() throws IOException {
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
            .returnProgram("return", a.clearAndAdd(b, c, -2, -1));
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test13(test1_this).dot");
    }

    @Test
    public void test14() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test14(test1_this).dot");
    }

    @Test
    public void test15() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test15(test1_this).dot");
    }

    @Test
    public void test16() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test16(test1_this).dot");
    }

    @Test
    public void test17() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test17(test1_this).dot");
    }

    @Test
    public void test18() throws IOException {
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
            )
            .returnProgram("return", StripesVariable.TOP);
        StripesDomainTest.checkProgram(p, "analysis___untyped_test1.test18(test1_this).dot");
    }

    /*
    @Test
    public void testX() throws IOException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program.next("").next("").returnProgram("return");
        StripesDomainTest.checkProgram(p, "test1.dot");
    }*/

    @Test
    public void testImg1() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_TestImg1.main(TestImg1_this).dot");
    }

    @Test
    public void testImg2() throws IOException {
        final StripesVariable a = new StripesVariable("a");
        final StripesVariable b = new StripesVariable("b");
        final StripesVariable c = new StripesVariable("c");
        final StripesVariable d = new StripesVariable("d");
        final StripesVariable e = new StripesVariable("e");
        final StripesVariable f = new StripesVariable("f");

        //noinspection ConstantConditions
        Assert.assertThat("To implement!", false, is(true));

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

        StripesDomainTest.checkProgram(p, "analysis___untyped_TestImg2.main(TestImg2_this).dot");
    }

    @Test
    public void testImg3_1() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_TestImg3.main1(TestImg3_this).dot");
    }

    @Test
    public void testImg3_2() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_TestImg3.main2(TestImg3_this).dot");
    }

    @Test
    public void testImg3_3() throws IOException {
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
        StripesDomainTest.checkProgram(p, "analysis___untyped_TestImg3.main3(TestImg3_this).dot");
    }
}
