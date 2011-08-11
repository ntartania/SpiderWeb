/*
 * File:         RandomLocationTransformer.Java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 * Takes ideas from the JUNG Project
 * @see http://jung.sourceforge.net/
 */
package spiderweb.visualizer.transformers;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Date;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

/**
 * Transforms the input type into a random location within
 * the bounds of the Dimension property.
 * This is used as the backing Transformer for the LazyMap
 * for many Layouts,
 * and provides a random location for unmapped vertex keys
 * the first time they are accessed.
 * 
 * @author Tom Nelson
 *
 * @param <V>
 */
public class RandomLocationTransformer<V> implements Transformer<V,Point2D> {

	Dimension d;
	Random random;
    
    public RandomLocationTransformer(Dimension d) {
    	this(d, new Date().getTime());
    }
    
    public RandomLocationTransformer(final Dimension d, long seed) {
    	this.d = d;
    	this.random = new Random(seed);
    }
    
    public Point2D transform(V v) {
        return new Point2D.Double(random.nextDouble() * d.width, random.nextDouble() * d.height);
    }
}
