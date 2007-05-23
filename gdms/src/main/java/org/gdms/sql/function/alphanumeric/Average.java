/***********************************
 * <p>Title: CarThema</p>
 * Perspectives Software Solutions
 * Copyright (c) 2006
 * @author Vladimir Peric, Vladimir Cetkovic
 ***********************************/

package org.gdms.sql.function.alphanumeric;

import org.gdms.data.values.DoubleValue;
import org.gdms.data.values.FloatValue;
import org.gdms.data.values.IntValue;
import org.gdms.data.values.LongValue;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.sql.function.Function;
import org.gdms.sql.function.FunctionException;



/**
 * @author Vladimir Peric
 */
public class Average implements Function{

    private Value average = null;
    private int num = 0;
    private double sum = 0;
    private int valueType = 0;
    
    /**
     * @see com.hardcode.gdbms.engine.function.Function#evaluate(com.hardcode.gdbms.engine.values.Value[])
     */
    public Value evaluate(Value[] args) throws FunctionException {
     
    	try {
    		
    		if(null == average){
    			valueType = args[0].getType();
    			double d = 0.0d;
				average = ValueFactory.createValue(d);
    		}
    		
    		switch(valueType){
    			case 3:
					sum += (double)(((LongValue)args[0]).getValue()); 
					((DoubleValue)average).setValue(sum/(++num));
					break;
    			case 4:
					sum += (double)(((IntValue)args[0]).getValue()); 
					((DoubleValue)average).setValue(sum/(++num));
					break;
				case 6:
				case 7:
					sum += (double)(((FloatValue)args[0]).getValue()); 
					((DoubleValue)average).setValue(sum/(++num));
					break;
				case 8:
					sum += (double)(((DoubleValue)args[0]).getValue()); 
					((DoubleValue)average).setValue(sum/(++num));
					break;
    		}
        } catch (Exception e) {
    		throw new FunctionException(e);
        }
        
        return average;
    }

    /**
     * @see com.hardcode.gdbms.engine.function.Function#getName()
     */
    public String getName() {
        return "Average";
    }

    /**
     * @see com.hardcode.gdbms.engine.function.Function#isAggregate()
     */
    public boolean isAggregate() {
        return true;
    }

    /**
     * @see com.hardcode.gdbms.engine.function.Function#cloneFunction()
     */
    public Function cloneFunction() {
        return new Average();
    }

	public int getType(int[] types) {
		
		return types[0];
	}

}
