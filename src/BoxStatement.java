

public class BoxStatement {
    private String operation;
    private String functionName;
    private String primitive;
    private int opCount;
    private int lineNum;

    public BoxStatement(String operation, String functionName, String primitive, int lineNum)
    {
        this.operation = operation;
        this.functionName = functionName;
        this.primitive = primitive;
        this.opCount = 0;
        this.lineNum = lineNum;
    }

    public int getLineNum()
    {
        return lineNum;
    }

    public void addOp()
    {
        opCount++;
    }

    public String getOperation()
    {
        return this.operation;
    }

    public int getOpCount()
    {
        return this.opCount;
    }

    public String getFunctionName()
    {
        return this.functionName;
    }

    public String getPrimitive()
    {
        return this.primitive;
    }

    public String toString()
    {
        return functionName + operation + opCount + primitive;
    }

}
