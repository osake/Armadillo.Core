package Web.Dashboard;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Text.StringWrapper;
import  Utils.Gui.AUiItem;
import Web.Base.LiveGuiPublisher;
import Web.Base.WebHelper;

public abstract class ATreeBean 
{
	private TreeNode m_selectedNode;
	private DefaultTreeNode m_rootTreeNode;
	private Hashtable<String, TreeNode> m_leafNodes;
	private ThreadWorker<StringWrapper> m_threadWorder;
	protected static Object m_lockObj = new Object();
	
	public ATreeBean()
	{
		try
		{
			m_leafNodes = new Hashtable<String, TreeNode>();
			generateTreeNodes();
			String strMessage = "Bean [" + getClass().getName() + "] is created";
			Logger.log(strMessage);
			Console.writeLine(strMessage);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private void generateTreeNodes() 
	{
		try 
		{
			m_rootTreeNode = new DefaultTreeNode(null, null);
			m_rootTreeNode.setData("Root");

			loadTreeNodes();
			loadTreeNodesWorker();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	private void loadTreeNodesWorker() 
	{
		try
		{
			m_threadWorder = new ThreadWorker<StringWrapper>()
			{
				@Override
				public void runTask(StringWrapper item) 
				{
					while(true)
					{
						try
						{
							loadTreeNodes();
						}
						catch(Exception ex)
						{
							Logger.log(ex);
						}
						finally
						{
							try
							{
								Thread.sleep(5000);
							}
							catch(Exception ex)
							{
								Logger.log(ex);
							}
						}
					}
				}
			};
			m_threadWorder.work();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	protected void loadTreeNodes() 
	{
		try
		{
			synchronized(m_lockObj)
			{
				for (Entry<String, AUiItem> kvp : 
					LiveGuiPublisher.getOwnInstance().getGuiItems().entrySet()) 
				{
					String strKey = kvp.getKey();
					addToLeafNodes(strKey);
				}
			}
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	protected boolean addToLeafNodes(String strKey) 
	{
		try
		{
			if(!m_leafNodes.containsKey(strKey))
			{
				TreeNode leafNode = 
						WebHelper.parseTree(strKey, m_rootTreeNode);
				
				if(leafNode != null)
				{
					m_leafNodes.put(strKey, leafNode);
					return true;
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
	
	public TreeNode getSelectedNode() 
	{
		return m_selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) 
	{
		m_selectedNode = selectedNode;
	}
	
	public DefaultTreeNode getRoot() 
	{
		return m_rootTreeNode;
	}

	public void setRoot(DefaultTreeNode root) 
	{
		m_rootTreeNode = root;
	}

	public void selectNode(String strTabName) 
	{
		try 
		{
			if (m_leafNodes.containsKey(strTabName)) 
			{
				m_leafNodes.get(strTabName).setSelected(true);
				for (Entry<String, TreeNode> kvp : m_leafNodes.entrySet()) 
				{
					if (!kvp.getKey().equals(strTabName)) 
					{
						kvp.getValue().setSelected(false);
					}
				}
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
}
