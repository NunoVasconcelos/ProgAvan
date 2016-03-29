
import javassist.*;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class MethodFinder {

	static List<BoxStatement> BoxUnboxList = new ArrayList<BoxStatement>();
	static BoxStatement testBox;

	public static void main(String[] args) throws CannotCompileException
	{
        try
		{

            //create the ClassPool
            ClassPool pool = ClassPool.getDefault();
            
            //Get class to be instrumented
            CtClass cc = pool.get(args[0]);

            for (CtMethod ctMethod : cc.getDeclaredMethods()){

                ctMethod.instrument(
                        new ExprEditor() {
                            public void edit(MethodCall m) throws CannotCompileException
                            {
                            	int lineNum = m.getLineNumber();
                            	String src = "";
								//condition to detect boxing because when exist boxing the method valueOf is always called
                                if(m.getMethodName().equals("valueOf"))//Detects a boxing
                                {
									//if the method is main
									if(ctMethod.getName().equals("main"))
									{
										System.err.print(ctMethod.getLongName());

										System.err.print(" boxed 1 ");
										try
										{
											//return the java language type of the returned value
											System.err.println(m.getMethod().getReturnType().getName());
										}
										catch (NotFoundException e)
										{
											e.printStackTrace();
										}
									}
									else try
									{
										//if is another method we need to compare the type of the parameter and
										//the type of returned value, and if is, is a boxing, if not, isn't a boxing
										if(ctMethod.getLongName().toLowerCase().endsWith(m.getMethod().getReturnType().getName()+")"))
										{
											System.err.print(ctMethod.getLongName());

											System.err.print(" boxed 1 ");
											try
											{
												System.err.println(m.getMethod().getReturnType().getName());
											}
											catch (NotFoundException e)
											{
												e.printStackTrace();
											}
										}
									} catch (NotFoundException e)
									{
										e.printStackTrace();
									}
									try
									{
										testBox = updateStatements("boxed", ctMethod.getLongName(), m.getMethod().getReturnType().getName(), lineNum);
									}
									catch(NotFoundException e)
									{
										e.printStackTrace();
									}
                                }
								//condition to detect unboxing because when exist unboxing the method like intValue
								//or longValue, so its always called a method that ends with Value
                                else if(m.getMethodName().endsWith("Value"))//Detect an unboxing
                                {
									if(ctMethod.getName().equals("main"))
									{
										try
										{
											testBox = updateStatements("unboxed", ctMethod.getLongName(), getType(m.getMethod().getReturnType().getName()), lineNum);
										}
										catch (NotFoundException e)
										{
											e.printStackTrace();
										}
									} else try
									{
										if(ctMethod.getLongName().toLowerCase().endsWith(m.getMethod().getReturnType().getName()+")")){
											try
											{
												testBox = updateStatements("unboxed", ctMethod.getLongName(), getType(m.getMethod().getReturnType().getName()), lineNum);
											}
											catch (NotFoundException e)
											{
												e.printStackTrace();
											}
										}
									}
									catch (NotFoundException e)
									{
										e.printStackTrace();
									}
                                }
                            }
                        });
            }

			//Sorting the list
			Collections.sort(BoxUnboxList,new Comparator<BoxStatement>() {
				@Override
				public int compare(BoxStatement a, BoxStatement b) {

					int result = b.getFunctionName().compareTo(a.getFunctionName());

					if (result == 0) {
						result = b.getPrimitive().compareTo(a.getPrimitive());
					}

					return result;
				}
			});

			//Inserting the code
			for (CtMethod ctMethod : cc.getDeclaredMethods())
			{
				String src;
				for(BoxStatement box : BoxUnboxList)
				{
					src = box.getFunctionName() + " " + box.getOperation() + " " + box.getOpCount();
					src += " " + box.getPrimitive();
					ctMethod.insertAt(box.getLineNum(), "System.err.println(\""+src+"\");");//Insert segments of code
					try
					{
						cc.writeFile(".");
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
					catch(CannotCompileException e)
					{
						e.printStackTrace();
					}
					cc.defrost();
				}
				if(BoxUnboxList.isEmpty())
				{
					try
					{
						cc.writeFile(".");
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
					catch(CannotCompileException e)
					{
						e.printStackTrace();
					}
					cc.defrost();
				}
				break;
			}
        }
		catch (IllegalArgumentException e)
		{
            e.printStackTrace();
        }
		catch (NotFoundException e)
		{
            e.printStackTrace();
        }
    }

	//Method to update the ArrayList to determine how many boxing/unboxing operations have been done in each primitive
	public static BoxStatement updateStatements(String operation, String functionName, String primitive, int lineNum)
	{
		boolean newBox = true;
		BoxStatement actualBox = new BoxStatement("", "", "", lineNum);		//Box that we are going to manipulate

		for(BoxStatement box: BoxUnboxList)
		{
			//Checks the list to find previous boxing/unboxing operation of a primitive in the given function
			if(box.getOperation().equals(operation))
				if(box.getFunctionName().equals(functionName))
					if(box.getPrimitive().equals(primitive))
					{
						newBox = false;
						actualBox = box;
					}
		}

		if(newBox)	//if newBox is true, there is not a BoxStatement like this on the list
		{
			actualBox = new BoxStatement(operation, functionName, primitive, lineNum);
			BoxUnboxList.add(actualBox);
		}
		actualBox.addOp();

		return actualBox;
	}

	//Method responsable for print a java language type based on a primitive type
	public static String getType(String type){
		switch (type){
			case "int": return "java.lang.Integer";
			case "long": return "java.lang.Long";
			case "char": return "java.lang.Character";
			case "byte": return "java.lang.Byte";
			case "short": return "java.lang.Short";
			case "double": return "java.lang.Double";
			case "float": return "java.lang.Float";
			case "boolean": return "java.lang.Boolean";
		}
		return "";
	}
}