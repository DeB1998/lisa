package it.unive.lisa.test.stripes;

import it.unive.lisa.test.stripes.cfg.program.EndedProgram;
import it.unive.lisa.test.stripes.cfg.program.For;
import it.unive.lisa.test.stripes.cfg.program.If;
import it.unive.lisa.test.stripes.cfg.program.IfBlockWithElse;
import it.unive.lisa.test.stripes.cfg.program.Program;
import it.unive.lisa.test.stripes.cfg.program.While;
import java.io.IOException;
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
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("c = 1")
            .next("d = 4")
            .next("e = c + d")
            .next(
                new If<StripesVariable>("e > 6")
                    .thenBlock()
                    .next("e = 10")
                    .next("f = 1")
                    .elseBlock()
                    .next("e = 2*(d+c)")
                    .next("f = 3")
            )
            .next(
                new If<StripesVariable>("e > 6")
                    .thenBlock()
                    .next("e = 5*(d + c)")
                    .next("f = 1")
                    .elseBlock()
                    .next("e = 2*(d+c)")
                    .next("f = 3")
            )
            .next(
                new If<StripesVariable>("e > 6")
                    .thenBlock()
                    .next("e = 2*(d + c)")
                    .next("f = 1")
                    .elseBlock()
                    .next("e = 2*(d+c)")
                    .next("f = 3")
            )
            .next(
                new If<StripesVariable>("e > 6")
                    .thenBlock()
                    .next("e = 2*(d + c) + 5")
                    .next("f = 1")
                    .elseBlock()
                    .next("e = 2*(d + c) + 3")
                    .next("f = 3")
            )
            .returnProgram("return");
        StripesDomainTest.checkProgram(p, "test1.dot");
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
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 1")
            .next("b = 2")
            .next("c = 3")
            .next("d = 10")
            .next(
                new For<StripesVariable>()
                    .initialization("i = 0")
                    .condition("i < c")
                    .next(
                        new While<StripesVariable>("a + b > 0")
                            .next(
                                new If<StripesVariable>("d > 10")
                                    .thenBlock()
                                    .next("d = d - 1")
                                    .next("b = d")
                                    .next("e = a + b")
                                    .elseBlock()
                                    .next("f = 5")
                                    .next("e = a + b")
                            )
                            .next("g = 3")
                    )
                    .increment("i = i + 1")
            )
            .returnProgram("return");
        StripesDomainTest.checkProgram(p, "test1.dot");
    }

    @Test
    public void test12() throws IOException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 1")
            .next("b = 2")
            .next("c = 3")
            .next("d = a + b")
            .next("e = b + c")
            .next("f = a + c")
            .next(
                new For<StripesVariable>()
                    .initialization("i = d")
                    .condition("i < e")
                    .next("g = d + f")
                    .next(
                        new If<StripesVariable>("g >= 1")
                            .thenBlock()
                            .next("g = d + 1")
                            .next(
                                new If<StripesVariable>("i == 0")
                                    .thenBlock()
                                    .next("f = 0")
                                    .next("g = d + 1")
                            )
                            .next("h = 9")
                            .elseBlock()
                            .next("g = d + 3")
                            .next(
                                new If<StripesVariable>("a != 0")
                                    .thenBlock()
                                    .next("f = a + b + 1")
                                    .elseBlock()
                                    .next("f = a + b + 10")
                                    .next("g = (3*d + 6)/3 + 1")
                            )
                    )
                    .increment("i = i + 1")
            )
            .returnProgram("return");
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

    @Test
    public void testX() throws IOException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program.next("").next("").returnProgram("return");
        StripesDomainTest.checkProgram(p, "test1.dot");
    }

    @Test
    public void testImg1() throws IOException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program
            .next("a = 10")
            .next("b = 5")
            .next("c = 1")
            .next(
                new If<StripesVariable>("a > b")
                    .thenBlock()
                    .next("c = a - b")
                    .elseBlock()
                    .next("c = 0")
            )
            .returnProgram("return c");
        StripesDomainTest.checkProgram(p, "test1.dot");
    }

    @Test
    public void testImg2() throws IOException {
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

    @Test
    public void testImg3() throws IOException {
        final Program<StripesVariable> program = new Program<>();
        final EndedProgram<StripesVariable> p = program.next("").next("").returnProgram("return");
        StripesDomainTest.checkProgram(p, "test1.dot");
    }
}
