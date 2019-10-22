package sjm.engine;

/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/

/**  
 * A Query is a dynamic rule that stands outside of a program 
 * and proves itself by referring to a program.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0  
 */
public class Query extends DynamicRule {
/**
 * Create a query from the given structures, to prove itself
 * against the given axiom source.
 *
 * @param   AxiomSource   the source to prove against
 *
 * @param   Structures   the structures to prove
 */
public Query(AxiomSource as, Structure[] structures) {
	this(as, new Scope(structures), structures);
}
/**
 * Create a query from the given rule's structures, to prove 
 * itself against the given axiom source.
 *
 * @param   AxiomSource   the source to prove against
 *
 * @param   Rule   the rule that contains structures to prove
 */
public Query(AxiomSource as, Rule rule) {
	this(as, rule.structures);
}
/*
 * This constructor ensures that the structures in the query
 * are all "provable", meaning that they are capable of
 * proving themselves. Structures cannot achieve this, but
 * they can produce consulting versions of themselves, given
 * an axiom source. Evalutations and comparisons are
 * provable in themselves, and will ignore the axiom
 * source.
 */
protected Query(
	AxiomSource as, Scope scope, Structure[] structures) {
		
	super(
		as, scope, provableStructures(as, scope, structures));
}
/**
 * Create a query from the given structure, to prove itself
 * against the given axiom source.
 *
 * @param   AxiomSource   the source to prove against
 *
 * @param   Structures   the structure to prove
 */
public Query(AxiomSource as, Structure structure) {
	this(as, new Structure[]{structure});
}
/**
 * Create a query from the given structure, to prove itself
 * without any axiom source.
 *
 * For example new Query(new Comparison())
 *
 * @param   AxiomSource   the source to prove against
 *
 * @param   Structure   the structure to prove
 */
public Query(Structure structure) {
	this(null, new Scope(), new Structure[]{structure});
}
/**
 * Returns a string representation of this query. 
 *
 * @return a string representation of this query.
 */
public String toString() {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < structures.length; i++) {
		if (i > 0) {
			buf.append(", ");
		}
		buf.append(structures[i].toString());
	}
	return buf.toString();
}
}