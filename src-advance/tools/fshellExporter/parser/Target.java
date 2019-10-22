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
package tools.fshellExporter.parser;

import java.util.ArrayList;
import java.util.Set;

import sjm.utensil.PubliclyCloneable;

public class Target implements PubliclyCloneable {

  private ArrayList queue = new ArrayList();
  

  private boolean blocked;


private int dereferences;
  


  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public Object top() {
//	  System.out.println("top queue : "+queue.toString());
//		System.out.flush();
    return queue.get(queue.size() - 1);
  }

  public Object pop()  {
	//  System.out.println("Queue :\n"+queue.toString());
//	  System.out.println("POP "+" "+System.identityHashCode(this));
//	System.out.println(  Thread.currentThread().getStackTrace()[2].getClassName() + " " +Thread.currentThread().getStackTrace()[2].getMethodName() + Thread.currentThread().getStackTrace()[2].getLineNumber() );
//	System.out.println("pop queue : "+queue.toString());
//	System.out.flush();
	Object o = queue.get(queue.size() - 1);
    queue.remove(queue.size() - 1);
    return o;
  }

  
  public int queueSize(){
	  return queue.size();
  }
  
  public void put(Object key,Object value) {
	//  System.out.println("#Target.put: key: "+key+" : "+value+" a "+memory.keySet() );
    //memory.put(key,value);
	  
	  IOMemoryRegistry.getInstance().getCurrentMethodsMap().put(key,value);
  }
  
  /**
   * Returns the original value of an inspector.
   * This method is called when evaluating an orig(..) operator during exit from a method.
   * 
   * @param key
   * @return
   */
  public Object getOrig(Object key) {
	  //System.out.println("#Target.get: key: "+key+" "+IOMemoryRegistry.getInstance().getCurrentMethodsMap().get(key) );
	  //System.out.println(memory);
      //return memory.get(key);
	  return IOMemoryRegistry.getInstance().getCurrentMethodsMap().get(key);
  }
  
  public Object remove(Object key) {
	  //System.out.println("#Target.remove: key: "+key );
	  //System.out.println(IOMemoryRegistry.getInstance().getCurrentMethodsMap().containsKey(key) );
	  //return memory.remove(key);
	  return IOMemoryRegistry.getInstance().getCurrentMethodsMap().remove(key);
  }
  
  public void push(Object o) {
	  //FIXME: throw an exception?
	  
//	  System.out.println("PUSHING "+o+" "+o.getClass().getCanonicalName()+" "+System.identityHashCode(this));
    queue.add(o);
//    System.out.println(  Thread.currentThread().getStackTrace()[2].getClassName() + " " +Thread.currentThread().getStackTrace()[2].getMethodName() + Thread.currentThread().getStackTrace()[2].getLineNumber() );
//	System.out.println("push queue : "+queue.toString());
//	System.out.flush();
  }
  
  
  public Object clone() {
//	  System.out.println("CLONE");
    try {
      Target t = (Target)super.clone();
      t.queue = (ArrayList)queue.clone();
      t.blocked = blocked;
      return t;
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  
  public boolean contains( Object key ){
	  return IOMemoryRegistry.getInstance().getCurrentMethodsMap().containsKey(key);
  }
  
  
  public String toString(){
	  String res = super.toString();
	  res += "queue: "+queue.toString();
	  return res;
  }
  
  /**
   * Block target queue, from this moment queue will be void an no push can be made.
   * It is used when a NonExistentMethod is called.
   *
   */
  public void block(){
	  //System.out.println("BLOCK");
	  blocked = true;
  }
  
  
  public boolean isBlocked(){
	  //System.out.println("BLOCKED "+blocked);
	  return blocked;
  }

  public Set getOrigKeys(){
	  return IOMemoryRegistry.getInstance().getCurrentMethodsMap().keySet();
  }


}
