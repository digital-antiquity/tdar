package org.tdar.transform;

public interface Transformer<S, R> {
	
	public R transform(S source);

}
