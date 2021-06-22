package it.unive.lisa.test.stripes.cfg.program;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-21
 * @since version date
 */
public class Program<V extends Variable<V>> {

    private ProgramCodeBlock<V> programCodeBlock;

    public Program() {
        this.programCodeBlock = new ProgramCodeBlock<>();
    }

    public Program<V> next(V... variables) {
        this.programCodeBlock.next(variables);
        return this;
    }

    public Program<V> next(String identifier, V... variables) {
        this.programCodeBlock.next(identifier, variables);
        return this;
    }

    public Program<V> next(CodeBlock<V> codeBlock) {
        this.programCodeBlock.next(codeBlock);
        return this;
    }

    public EndedProgram<V> returnProgram(V... variables) {
        return this.returnProgram("return", variables);
    }

    public EndedProgram<V> returnProgram(String identifier, V... variables) {
        this.programCodeBlock.next(identifier, variables);
        return this.programCodeBlock;
    }
}
