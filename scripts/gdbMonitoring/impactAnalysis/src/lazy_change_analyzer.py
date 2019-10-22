'''
Created on Mar 4, 2011

@author: fabrizio
'''
import os
import sys
import getopt
import pydot
import pydot
import string


def get_children( graph, nodeName):
    result = []
    for edge in graph.get_edges():
        src = edge.get_source()
        if src is None:
            continue
        if string.find ( src , nodeName ) != -1 :
            dest = edge.get_destination()
            if  dest is None:
                continue
            result.append( dest )
        
    return set ( result )

def get_parents( graph, nodeName):
    result = []
    for edge in graph.get_edges():
        dest = edge.get_destination()
        if dest is None:
            continue
        if string.find ( dest , nodeName ) != -1 :
            src = edge.get_source()
            if  src is None:
                continue
            result.append( src )
        
    return set ( result )
        

def read_target_nodes ( file_with_nodes ) :
    file = open ( file_with_nodes, 'r' )
    lines = list()
    for line in file.readlines():
        lines.append( string.strip(line) )
    file.close()
    return lines


def write_to_file ( list, fileName ):
    file = open ( fileName, 'w' )
    for el in list :
        file.write(string.strip(el, '"'))
        file.write("\n")
    file.close()


def get_nodes ( graph, nodeName ):
    result = [];
    for node in graph.get_nodes() :
        if string.find ( node.get_name() , nodeName ) != -1 :
            result.append(nodeName)
    return result

def main( argv = sys.argv ):
    graph = pydot.graph_from_dot_file(argv[1])
   
    
    nodesToVisit = []
    allParents = []
    allChildren = []
    
    targetNodes = read_target_nodes( argv[2] )
    #identify callers, callee and siblings of node
    #for i in range( 2, argv.__len__() ) :
    #    nodeName = "\""+argv[i]+"\""
    for targetNode in targetNodes :
        for nodeName in get_nodes ( graph, targetNode ) :
            print "nodeName : "+nodeName
            #methods that are siblings of node
            for parent in get_parents ( graph, nodeName ) :
                nodesToVisit.append(parent)
                allParents.append(parent)
                children = get_children ( graph, parent )
                for child in children:
                    allChildren.append(child)
                    nodesToVisit.append(child)
                #methods that are children of node
                children = get_children ( graph, nodeName )
                for child in children:
                    nodesToVisit.append(child)
                    allChildren.append(child)
            
            
    write_to_file( set(allChildren), "bct.impact.children.txt" )
    write_to_file( set(allParents), "bct.impact.parents.txt" )
                           
    visitSet = set( nodesToVisit )
    
    for function in visitSet :
        print string.strip(function,'"')   
      
       
    
if __name__ == '__main__':
    main()
