package dsa_assignment3;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A node in a binary tree representing a Propositional Logic expression.
 * <p>
 * Each node has a type (AND node, OR node, NOT node etc.) and can have zero one or two children as required by the node type (AND has two
 * children, NOT has one, TRUE, FALSE and variables have none
 * </p>
 * <p>
 * This class is mutable, and some of the operations are intended to modify the tree internally. There are a few cases when copies need to
 * be made of whole sub-tree nodes in such a way that these copied trees do not share any nodes with their originals. To do this the class
 * implements a deep copying copy constructor <code>PLTreeNode(PLTreeNode node)</code>
 * </p>
 * 
 */
public final class PLTreeNode implements PLTreeNodeInterface
{
	private static final Logger	logger	= Logger.getLogger(PLTreeNode.class);

	NodeType					type;
	PLTreeNode					child1;
	PLTreeNode					child2;

	/**
	 * For marking purposes
	 * 
	 * @return Your student id
	 */
	public static String getStudentID()
	{
		//change this return value to return your student id number
		return "1911437";
	}

	/**
	 * For marking purposes
	 * 
	 * @return Your name
	 */
	public static String getStudentName()
	{
		//change this return value to return your name
		return "TOM MOSES";
	}

	/**
	 * The default constructor should never be called and has been made private
	 */
	private PLTreeNode()
	{
		throw new RuntimeException("Error: default PLTreeNode constuctor called");
	}

	/**
	 * The usual constructor used internally in this class. No checks made on validity of parameters as this has been made private, so can
	 * only be called within this class.
	 * 
	 * @param type
	 *            The <code>NodeType</code> represented by this node
	 * @param child1
	 *            The first child, if there is one, or null if there isn't
	 * @param child2
	 *            The second child, if there is one, or null if there isn't
	 */
	private PLTreeNode(NodeType type, PLTreeNode child1, PLTreeNode child2)
	{
		// Don't need to do lots of tests because only this class can create nodes directly,
		// any construction required by another class has to go through reversePolishBuilder,
		// which does all the checks
		this.type = type;
		this.child1 = child1;
		this.child2 = child2;
	}

	/**
	 * A copy constructor to take a (recursive) deep copy of the sub-tree based on this node. Guarantees that no sub-node is shared with the
	 * original other
	 * 
	 * @param node
	 *            The node that should be deep copied
	 */
	private PLTreeNode(PLTreeNode node)
	{
		if (node == null)
			throw new RuntimeException("Error: tried to call the deep copy constructor on a null PLTreeNode");
		type = node.type;
		if (node.child1 == null)
			child1 = null;
		else
			child1 = new PLTreeNode(node.child1);
		if (node.child2 == null)
			child2 = null;
		else
			child2 = new PLTreeNode(node.child2);
	}

	/**
	 * Takes a list of <code>NodeType</code> values describing a valid propositional logic expression in reverse polish notation and
	 * constructs the corresponding expression tree. c.f.
	 * <a href= "https://en.wikipedia.org/wiki/Reverse_Polish_notation">https://en.wikipedia.org/wiki/Reverse_Polish_notation</a>
	 * <p>
	 * Thus an input containing
	 * </p>
	 * 
	 * <pre>
	 * {NodeType.P, NodeType.Q, NodeType.NOT, NodeType.AND.
	 * </pre>
	 * 
	 * corresponds to
	 * 
	 * <pre>
	 * P∧¬Q
	 * </pre>
	 * 
	 * Leaving out the <code>NodeType</code> enum class specifier, we get that
	 * 
	 * <pre>
	 * { R, P, OR, TRUE, Q, NOT, AND, IMPLIES }
	 * </pre>
	 * 
	 * represents
	 * 
	 * <pre>
	 * ((R∨P)→(⊤∧¬Q))
	 * </pre>
	 * 
	 * @param typeList
	 *            An <code>NodeType</code> array in reverse polish order
	 * @return the <code>PLTreeNode</code> of the root of the tree representing the expression constructed for the reverse polish order
	 *         <code>NodeType</code> array
	 */
	public static PLTreeNodeInterface reversePolishBuilder(NodeType[] typeList)
	{
		if (typeList == null || typeList.length == 0)
		{
			logger.error("Trying to create an empty PLTree");
			return null;
		}

		Deque<PLTreeNode> nodeStack = new LinkedList<>();

		for (NodeType type : typeList)
		{
			int arity = type.getArity();

			if (nodeStack.size() < arity)
			{
				logger.error(String.format(
						"Error: Malformed reverse polish type list: \"%s\" has arity %d, but is being applied to only %d arguments", //
						type, arity, nodeStack.size()));
				return null;
			}
			if (arity == 0)
				nodeStack.addFirst(new PLTreeNode(type, null, null));
			else if (arity == 1)
			{
				PLTreeNode node1 = nodeStack.removeFirst();
				nodeStack.addFirst(new PLTreeNode(type, node1, null));
			}
			else
			{
				PLTreeNode node2 = nodeStack.removeFirst();
				PLTreeNode node1 = nodeStack.removeFirst();
				nodeStack.addFirst(new PLTreeNode(type, node1, node2));
			}
		}
		if (nodeStack.size() > 1)
		{
			logger.error("Error: Incomplete term: multiple subterms not combined by top level symbol");
			return null;
		}

		return nodeStack.removeFirst();
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#getReversePolish()
	 */
	@Override
	public NodeType[] getReversePolish()
	{
		Deque<NodeType> nodeQueue = new LinkedList<>();

		getReversePolish(nodeQueue);

		return nodeQueue.toArray(new NodeType[0]);

	}

	/**
	 * A helper method for <code>getReversePolish()</code> used to accumulate the elements of the reverse polish notation description of the
	 * current tree
	 * 
	 * @param nodeQueue
	 *            A queue of <code>NodeType</code> objects used to accumulate the values of the reverse polish notation description of the
	 *            current tree
	 */
	private void getReversePolish(Deque<NodeType> nodeQueue)
	{
		if (child1 != null)
			child1.getReversePolish(nodeQueue);
		if (child2 != null)
			child2.getReversePolish(nodeQueue);
		nodeQueue.addLast(type);
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#toString()
	 */
	@Override
	public String toString()
	{
		return toStringPrefix();
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#toStringPrefix()
	 */
	@Override
	public String toStringPrefix()
	{
		String text = this.type.getPrefixName();
		if (!this.type.isVar())
		{
			if (this.type.getArity() == 1)
			{
				text += "(" + this.child1.toStringPrefix() + ")";
			}
			else if (this.type.getArity() == 2)
			{
				text += "(";
				text += this.child1.toStringPrefix();
				text += ",";
				text += this.child2.toStringPrefix();
				text += ")";
			}
		}
		return text;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#toStringInfix()
	 */
	@Override
	public String toStringInfix()
	{
		String text = "error";
		if (this.type.getArity() == 0)
		{
			text = this.type.getInfixName();
		}
		else
		{
			if (this.type.getArity() == 1)
			{
				text = this.type.getInfixName();
				text += this.child1.toStringInfix();
			}
			else if (this.type.getArity() == 2)
			{
				text = "(";
				text += this.child1.toStringInfix();
				text += this.type.getInfixName();
				text += this.child2.toStringInfix();
				text += ")";
			}
		}
		return text;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#applyVarBindings(java.util.Map)
	 */
	@Override
	public void applyVarBindings(Map<NodeType, Boolean> bindings)
	{
		if (this.type.getArity() == 0)
		{
			if (bindings.containsKey(this.type))
			{
				if (bindings.get(this.type) == true)
				{
					this.type = NodeType.TRUE;
				}
				else
				{
					this.type = NodeType.FALSE;
				}
			}
		}
		else if (this.type.getArity() == 1)
		{
			this.child1.applyVarBindings(bindings);
		}
		else if (this.type.getArity() == 2)
		{
			this.child1.applyVarBindings(bindings);
			this.child2.applyVarBindings(bindings);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#evaluateConstantSubtrees()
	 */
	@Override
	public Boolean evaluateConstantSubtrees()
	{
		// WRITE YOUR CODE HERE, CHANGING THE RETURN STATEMENT IF NECESSARY
		// for this recursive procedure we want to apply it to the children first and then to this.
		// we first define the map as shown at the top of page three of the assignment pdf.
		//Map<NodeType[], Map<NodeType, NodeType>> table = new HashMap<NodeType[], Map<NodeType, NodeType>>();
		// we now populate this map:
		//table.put(new NodeType[]{NodeType.TRUE,NodeType.TRUE}, new HashMap<NodeType, NodeType>());
		//table.get
		
		if (this.type.getArity() == 1)
		{
			this.child1.evaluateConstantSubtrees();
			if (this.type == NodeType.NOT)
			{
				if (this.child1.type == NodeType.TRUE) 
				{
					this.type = NodeType.FALSE;
					this.child1 = null;
				}
				else if (this.child1.type == NodeType.FALSE)
				{
					this.type = NodeType.TRUE;
					this.child1 = null;
				}
			}
		}
		else if (this.type.getArity() == 2)
		{
			this.child1.evaluateConstantSubtrees();
			this.child2.evaluateConstantSubtrees();
			
			//we then run the processing part of this algorithm
			
			if (this.child1.type.equals(NodeType.TRUE) && this.child2.type.equals(NodeType.TRUE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = NodeType.TRUE;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = NodeType.TRUE;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.TRUE;
				}
			}
			else if (this.child1.type.equals(NodeType.TRUE) && this.child2.type.equals(NodeType.FALSE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = NodeType.FALSE;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = NodeType.TRUE;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.FALSE;
				}
			}
			else if (this.child1.type.equals(NodeType.TRUE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = this.child2.type;
					this.child1 = this.child2.child1;
					this.child2 = this.child2.child2;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = NodeType.TRUE;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = this.child2.type;
					this.child1 = this.child2.child1;
					this.child2 = this.child2.child2;
				}
			}
			else if (this.child1.type.equals(NodeType.FALSE) && this.child2.type.equals(NodeType.TRUE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = NodeType.FALSE;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = NodeType.TRUE;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.TRUE;
				}
			}
			else if (this.child1.type.equals(NodeType.FALSE) && this.child2.type.equals(NodeType.FALSE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = NodeType.FALSE;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = NodeType.FALSE;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.TRUE;
				}
			}
			else if (this.child1.type.equals(NodeType.FALSE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = NodeType.FALSE;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = this.child2.type;
					this.child1 = this.child2.child1;
					this.child2 = this.child2.child2;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.TRUE;
				}
			}
			else if (this.child2.type.equals(NodeType.TRUE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = this.child1.type;
					this.child2 = this.child1.child2;
					this.child1 = this.child1.child1;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = NodeType.TRUE;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.TRUE;
				}
			}
			else if (this.child2.type.equals(NodeType.FALSE))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = NodeType.FALSE;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = this.child1.type;
					this.child2 = this.child1.child2;
					this.child1 = this.child1.child1;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = NodeType.NOT;
					this.child1 = new PLTreeNode(this.child1.type, this.child1.child1, this.child1.child2);
					this.child2 = null;
				}
			}
			else if (this.child1.toStringInfix().equals(this.child2.toStringInfix()))
			{
				if (this.type.equals(NodeType.AND))
				{
					this.type = this.child2.type;
					this.child1 = this.child2.child1;
					this.child2 = this.child2.child2;
				}
				else if (this.type.equals(NodeType.OR))
				{
					this.type = this.child2.type;
					this.child1 = this.child2.child1;
					this.child2 = this.child2.child2;
				}
				else if (this.type.equals(NodeType.IMPLIES))
				{
					this.type = this.child2.type;
					this.child1 = this.child2.child1;
					this.child2 = this.child2.child2;
				}
			}
			if (this.type.getArity() == 1)
			{
				this.child1.evaluateConstantSubtrees();
			}
			else if (this.type.getArity() == 2)
			{
				this.child1.evaluateConstantSubtrees();
				this.child2.evaluateConstantSubtrees();
			}
		}
		if (this.type.equals(NodeType.TRUE) || this.type.equals(NodeType.FALSE))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#reduceToCNF()
	 */
	@Override
	public void reduceToCNF()
	{
		eliminateImplies();
		pushNotDown();
		pushOrBelowAnd();
		makeAndOrRightDeep();
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#eliminateImplies()
	 */
	@Override
	public void eliminateImplies()
	{
		if (this.type.equals(NodeType.IMPLIES))
		{
			this.type = NodeType.OR;
			this.child1 = new PLTreeNode(NodeType.NOT, this.child1, null);
		}
		if (this.type.getArity() == 1) {
			this.child1.eliminateImplies();
		}
		else if (this.type.getArity() == 2) {
			this.child1.eliminateImplies();
			this.child2.eliminateImplies();
		}
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#pushNotDown()
	 */
	@Override
	public void pushNotDown()
	{
		
		if (this.type.equals(NodeType.NOT)) {
		
			if (this.child1.type.equals(NodeType.NOT)) {
				//this = this.child1.child1;
				this.type = this.child1.child1.type;
				this.child2 = this.child1.child1.child2;
				this.child1 = this.child1.child1.child1;
				this.pushNotDown();
			}
			
			else if (this.child1.type.equals(NodeType.OR)) {
				this.type = NodeType.AND;
				this.child2 = new PLTreeNode(NodeType.NOT, this.child1.child2, null);
				this.child1 = new PLTreeNode(NodeType.NOT, this.child1.child1, null);
				 this.pushNotDown();
			}
			 
			else if (this.child1.type.equals(NodeType.AND)) {
				this.type = NodeType.OR;
				this.child2 = new PLTreeNode(NodeType.NOT, this.child1.child2, null);
				this.child1 = new PLTreeNode(NodeType.NOT, this.child1.child1, null);
				this.pushNotDown();
			}
			else
			{
				this.child1.pushNotDown(); 
			}
			
		}
		else
		{
			if (this.type.getArity() == 1) {
				this.child1.pushNotDown();
			}
			else if (this.type.getArity() == 2) {
				this.child1.pushNotDown();
				this.child2.pushNotDown();
			} 
		}
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#pushOrBelowAnd()
	 */
	@Override
	public void pushOrBelowAnd()
	{
		if (this.type.getArity() == 1) 
		{
			this.child1.pushOrBelowAnd();
		}
		else if (this.type.getArity() == 2)
		{
			this.child1.pushOrBelowAnd();
			this.child2.pushOrBelowAnd();
		}
		if (this.type.equals(NodeType.OR)) {
			if (this.child2.type.equals(NodeType.AND))
			{
				this.type = NodeType.AND;
				PLTreeNode temp1 = new PLTreeNode(NodeType.OR, this.child1, this.child2.child1);
				PLTreeNode temp2 = new PLTreeNode(NodeType.OR, this.child1, this.child2.child2);
				this.child1 = new PLTreeNode(temp1);
				this.child2 = new PLTreeNode(temp2);
				this.pushOrBelowAnd();
			}
			else if (this.child1.type.equals(NodeType.AND))
			{
				this.type = NodeType.AND;
				PLTreeNode temp1 = new PLTreeNode(NodeType.OR, this.child1.child1, this.child2);
				PLTreeNode temp2 = new PLTreeNode(NodeType.OR, this.child1.child2, this.child2);
				this.child1 = new PLTreeNode(temp1);
				this.child2 = new PLTreeNode(temp2);
				this.pushOrBelowAnd();
			}
		}
		else if (this.type.getArity() == 1) 
		{
			this.child1.pushOrBelowAnd();
		}
		else if (this.type.getArity() == 2)
		{
			this.child1.pushOrBelowAnd();
			this.child2.pushOrBelowAnd();
		}
		return;
	}

	/* (non-Javadoc)
	 * @see dsa_assignment3.PLTreeNodeInterface#makeAndOrRightDeep()
	 */
	@Override
	public void makeAndOrRightDeep()
	{
		if (this.type.equals(NodeType.OR))
		{
			if (this.child1.type.equals(NodeType.OR))
			{
				this.child2 = new PLTreeNode(NodeType.OR, this.child1.child2, this.child2);
				this.child1 = this.child1.child1;
			}
		}
		else if (this.type.equals(NodeType.AND))
		{
			if (this.child1.type.equals(NodeType.AND))
			{
				this.child2 = new PLTreeNode(NodeType.AND, this.child1.child2, this.child2);
				this.child1 = this.child1.child1;
			}
		}
		if (this.type.getArity() == 1) 
		{
			this.child1.makeAndOrRightDeep();
		}
		else if (this.type.getArity() == 2)
		{
			this.child1.makeAndOrRightDeep();
			this.child2.makeAndOrRightDeep();
		}
		return;
	}

}
