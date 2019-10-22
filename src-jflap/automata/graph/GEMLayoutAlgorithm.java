/* -- JFLAP 4.0 --
 *
 * Copyright information:
 *
 * Susan H. Rodger, Thomas Finley
 * Computer Science Department
 * Duke University
 * April 24, 2003
 * Supported by National Science Foundation DUE-9752583.
 *
 * Copyright (c) 2003
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the author.  The name of the author may not be used to
 * endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
 
package automata.graph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** 
 * Implements the GEM algorithm, by Arne Frick, Andreas Ludwig, and
 * Heiko Mehldau in their 1994 paper.  At present the rotation
 * detection is not built in, as forcing speedier convergence is
 * totally unnecessary for our limited applications.
 * 
 * @author Thomas Finley
 */

public class GEMLayoutAlgorithm implements LayoutAlgorithm {
    public GEMLayoutAlgorithm() {
	
    }

    public void layout(Graph graph, Set isovertices) {
	if (isovertices == null) isovertices = EMPTY_SET;
	Object[] vArray = graph.vertices();
	int Rmax = 120 * (vArray.length - isovertices.size());
	double Tglobal = Tmin+1.0;

	// Determine an optimal edge length.  With isovertices, we
	// want optimal length to be about average of existing edges
	// that will remain unchanged due to isovertex status.
	double optimalEdgeLength = OPTIMAL_EDGE_LENGTH;
	if (isovertices.size() > 0) {
	    Object[] iso = isovertices.toArray();
	    int count = 0;
	    double lengths = 0.0;
	    for (int i=0; i<iso.length; i++)
		for (int j=i+1; j<iso.length; j++) {
		    if (!graph.hasEdge(iso[i], iso[j])) continue;
		    lengths += graph.pointForVertex
			(iso[i]).distance(graph.pointForVertex(iso[j]));
		    count++;
		}
	    if (count > 0)
		optimalEdgeLength = lengths / (double) count;
	}

	// The barycenter of the graph.
	double[] c = new double[] {0.0, 0.0};

	// Initialize the record for each vertex.
	records = new HashMap();
	for (int i=0; i<vArray.length; i++) {
	    Record r = new Record();
	    r.point = graph.pointForVertex(vArray[i]);
	    // The barycenter will be updated.
	    c[0] += r.point.getX();
	    c[1] += r.point.getY();
	    records.put(vArray[i], r);
	}

	// Iterate until done.
	ArrayList vertices = new ArrayList();
	for (int i=0; i<Rmax && Tglobal > Tmin; i++) {
	    if (vertices.isEmpty()) {
		vertices = new ArrayList();
		for (int j=0; j<vArray.length; j++)
		    if (!isovertices.contains(vArray[j]))
			vertices.add(vArray[j]);
		if (vertices.size() == 0) return;
	    }

	    // Choose a vertex V to update.
	    int index = RANDOM.nextInt(vertices.size());
	    Object vertex = vertices.remove(index);
	    Record record = (Record) records.get(vertex);
	    Point2D point = graph.pointForVertex(vertex);

	    // Compute the impulse of V.
	    double Theta = graph.degree(vertex);
	    Theta *= 1.0 + Theta/2.0;
	    double[] p = new double[] 
		{(c[0]/graph.numberOfVertices() - point.getX())*
		 GRAVITATIONAL_CONSTANT*Theta,
		 (c[1]/graph.numberOfVertices() - point.getY())*
		 GRAVITATIONAL_CONSTANT*Theta}; // Attraction to BC.
	    // Random disturbance.
	    p[0] += RANDOM.nextDouble() * 10.0 - 5.0;
	    p[1] += RANDOM.nextDouble() * 10.0 - 5.0;
	    // Forces exerted by other nodes.
	    for (int j=0; j<vArray.length; j++) {
		if (vArray[j] == vertex) continue;
		Point2D otherPoint = graph.pointForVertex(vArray[j]);
		double[] delta = new double[] 
		    {point.getX()-otherPoint.getX(),
		     point.getY()-otherPoint.getY()};
		double D2 = delta[0]*delta[0] + delta[1]*delta[1];
		double O2 = optimalEdgeLength*optimalEdgeLength;
		if (delta[0] != 0.0 || delta[1] != 0.0) {
		    for (int k=0; k<2; k++)
			p[k] += delta[k] * O2 / D2;
		}
		if (!graph.hasEdge(vertex, vArray[j])) continue;
		for (int k=0; k<2; k++)
		    p[k] -= delta[k] * D2 / (O2 * Theta);
	    }

	    // Adjust the position and temperature.
	    if (p[0] != 0.0 || p[1] != 0.0) {
		double absp = Math.sqrt(Math.abs(p[0]*p[0] + p[1]*p[1]));
		for (int j=0; j<2; j++)
		    p[j] *= record.temperature / absp;
		// update the position!
		graph.moveVertex
		    (vertex, new Point2D.Double(point.getX()+p[0], 
						point.getY()+p[1]));
		// update the barycenter
		c[0] += p[0]; c[1] += p[1];
	    }
	    // Adjust the temperature.
	    /*if (record.lastImpulse[0] != 0.0 || record.lastImpulse[1] != 0.0) {
		double beta = Math.atan2(p[0]-record.lastImpulse[0],
					 p[1]-record.lastImpulse[1]);
		if (Math.sin(beta) >= Math.sin(Math.PI/2.0 + alphaR
		}*/
	    
	    // Paint the component...
	    /*if ((i+1) % (vArray.length - isovertices.size()) == 0)
	      component.paintImmediately(component.getBounds());*/
	}
	
    }

    private static final Random RANDOM = new Random();
    private Map records;

    private static final Set EMPTY_SET = new HashSet();

    private static class Record {
	Point2D point = new Point2D.Double();
	double[] lastImpulse = {0.0,0.0};
	double temperature = Tmin;
	double skew = 0.0;
    }

    private static final double Tmax = 256.0, Tmin = 3.0;
    private static final double
	OPTIMAL_EDGE_LENGTH = 100.0,
	GRAVITATIONAL_CONSTANT = 1.0/16.0;
    /*private static final double
      alphaO */
}
