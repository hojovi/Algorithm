/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.ht.huaweicodecraft2016;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class Route_dfs
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
    public static String searchRoute(String graphContent, String condition)
    {
        Route_dfs route=new Route_dfs();
        return route.goTwo(graphContent, condition);
    }
    
    private HashMap<Integer,HashMap<Integer,Integer>> diGraph=new HashMap<>();	//所有的点对应的权重,有向箭头的头,有向箭头的尾,权重
    private HashMap<Integer,HashSet<Integer>> revDiGraph=new HashMap<>();	//所有点对应的权重,有向箭头的尾,有向箭头的头
	private HashMap<Integer,HashMap<Integer,Integer>> dict=new HashMap<>();		//所有的点对应的线段
	private Integer endPoint;
	private Integer startPoint;
	private HashSet<Integer> points=new HashSet<>();	//V'
	private HashSet<Integer> allPoints=new HashSet<>();
	
	//这种方法连最简单的case的最优解都算不出来,应该要优化,但是我觉得这个程序本身就要运行蛮长时间的,所以优化究竟可行吗以及如何优化都是个问题
	//留待之后考虑
	public String go(String graphContent, String condition){
		initialize(graphContent,condition);
		if(!trimAndSimpleJudge()){
			return "NA";
		}else{
			HashMap<Integer,Integer> path=find();
			if(path==null){
				return "NA";
			}else{
				Integer tmp=startPoint;
				StringBuilder sb=new StringBuilder();
				while(!tmp.equals(endPoint)){
					Integer next=path.get(tmp);
					sb.append(dict.get(tmp).get(next));
					sb.append('|');
					tmp=next;
				}
				sb.deleteCharAt(sb.length()-1);
				return sb.toString();
			}
		}
	}
	
	private void initialize(String graphContent, String condition){
		//graphContent和Condition最后都有个回车，真扯淡
		graphContent=graphContent.substring(0, graphContent.length()-1);
		condition=condition.substring(0, condition.length()-1);
		
		//初始化v'=points, 起点=startPoint, 终点=endPoint
		String[] strs=condition.split(",");
		startPoint=Integer.valueOf(strs[0]);
		endPoint=Integer.valueOf(strs[1]);
		strs=strs[2].split("\\|");
		for(String s:strs){
			points.add(Integer.valueOf(s));
		}
		
		
		String lines[]=graphContent.split("\\n");
		for(String line:lines){
			strs=line.split(",");
			Integer lineID=Integer.valueOf(strs[0]);
			Integer srcID=Integer.valueOf(strs[1]);
			Integer desID=Integer.valueOf(strs[2]);
			Integer value = Integer.valueOf(strs[3]);
			
			if(srcID.equals(desID)){
				continue;
			}
			
			//记录所有的点
			if(!allPoints.contains(srcID)){
				allPoints.add(srcID);
			}
			if(!allPoints.contains(desID)){
				allPoints.add(desID);
			}
			
			//将数据插入diGraph和dict
			if (!diGraph.containsKey(srcID)) {
				diGraph.put(srcID, new HashMap<Integer, Integer>());
				dict.put(srcID, new HashMap<Integer, Integer>());
			}
			HashMap<Integer, Integer> map = diGraph.get(srcID);
			HashMap<Integer, Integer> mapDict = dict.get(srcID);
			if (map.containsKey(desID)) {
				Integer preValue = map.get(desID);
				if (value < preValue) {
					map.put(desID, value);
					mapDict.put(desID, lineID);
				}
			} else {
				map.put(desID, value);
				mapDict.put(desID, lineID);
			}
			
			//初始化revDiGraph,后加上的
			if(!revDiGraph.containsKey(desID)){
				revDiGraph.put(desID, new HashSet<Integer>());
			}
			HashSet<Integer> mapRev=revDiGraph.get(desID);
			if(!mapRev.contains(srcID)){
				mapRev.add(srcID);
			}
		}
	}
	
	//试图递归删除除终点外出度为0的点,并做简单的判断,如果v'中有点出度为0,那肯定没有结果
	public boolean trimAndSimpleJudge(){
		while(allPoints.size()>0){
			Iterator<Integer> it=allPoints.iterator();
			Integer index=-1;		//应该删除的点
			while(it.hasNext()){
				Integer cur=it.next();
				if(!diGraph.containsKey(cur)&&!cur.equals(endPoint)){
					index=cur;
					break;
				}
			}
			//如果找到的是终点,那没什么意义
			if(index.equals(endPoint)){
				continue;
			}
			//如果找到一个在v'中的点,那就扯犊子了
			if(points.contains(index)){
				return false;
			}
			if(index==-1){
				//已经没有出度为0的点了
				return true;
			}else{
				//开始删除
				HashSet<Integer> revMap=revDiGraph.get(index);
				Iterator<Integer> itt=revMap.iterator();
				while(itt.hasNext()){
					Integer srcID=itt.next();
					diGraph.get(srcID).remove(index);
				}
				revDiGraph.remove(index);
				allPoints.remove(index);
			}
		}
		//程序运行到这里就更扯淡了,怎么没点了?
		return false;
	}
	
	private HashMap<Integer,Integer> find(){
		return find(points,new HashSet<Integer>(),startPoint);
	}
	
	//在这里进行迭代,找出距当前点最近的点,然后再找它的下一个最近的点,找不到的话回来找第二个
	//banned代表之后被禁止访问到的点,他可能是已经被访问到了,也可能是终点,这个函数不是每次找出一条路径吗,每次找出的路径除了终点外都应该被归入banned
	private HashMap<Integer,Integer> find(HashSet<Integer> points, HashSet<Integer> banned, Integer cur){
		//迭代截止条件,points为空
		if(points.isEmpty()){
			if(diGraph.get(cur).containsKey(endPoint)){
				HashMap<Integer,Integer> result=new HashMap<>();
				result.put(cur, endPoint);
				return result;
			}else{
				return null;
			}
		}
		
		ArrayDeque<HashMap<Integer,Integer>> deq=new ArrayDeque<>();
		
		//初始化deq
		HashMap<Integer, Integer> path=new HashMap<>();
		path.put(-1, cur);
		path.put(-2, 0);
		deq.addLast(path);
		
		while(findNearest(cur,points,banned,deq)){
			//找到最近的那条路了,加油,找下一条
			path=deq.peekLast();
			Integer tmp=cur;
			Integer lastPoint=path.get(-1);
			while(!tmp.equals(lastPoint)){
				banned.add(tmp);
				tmp=path.get(tmp);
			}
			points.remove(lastPoint);
			
			//这是找下一条
			HashMap<Integer,Integer> laterPath=find(points,banned,lastPoint);
			if(laterPath!=null){
				laterPath.putAll(path);
				return laterPath;
			}
			
			//没找到,把当前路径信息从visited和points数组里面删掉,再走
			tmp=cur;
			while(!tmp.equals(lastPoint)){
				banned.remove(tmp);
				tmp=path.get(tmp);
			}
			points.add(lastPoint);
		}
		return null;
	}
	
	//找到距离cur最近的在v'内的点, 输入banned是不被允许访问的点, set是v'集, 有点贪婪法的味道
	//如果最近的点不行怎么办,如何切换到第二近的点?????重算?我去nm的重算!!我也真是日了狗了,我X
	//思路啊思路,你在哪里? 不要抛弃我
	//要不然先保存下来? 就怕结果集太大,吓死个人嘞
	//不管了,先这样吧,把数据集保存下来
	//那输入输出就要有变化了,重构一下
	//下面是重构的结果,最初的是在本函数的下面
	//函数返回是否有结果
	private boolean findNearest(Integer cur, HashSet<Integer> set, HashSet<Integer> banned, ArrayDeque<HashMap<Integer,Integer>> deq){
		int min=0;
		HashMap<Integer, Integer> path;
		
		// 开始深度优先
		int size = 0;
		while ((size = deq.size()) > 0) {
			int curMin = 0x7fffffff;
			for (int i = 0; i < size; i++) {
				path = deq.removeFirst();
				Integer lastPoint = path.get(-1);
				int minu = path.get(-2);
				if (set.contains(lastPoint) && minu == min && !lastPoint.equals(cur)&&!banned.contains(lastPoint)) {
					deq.addLast(path);
					return true;
				}
				assert (minu >= min); // TODO remember to delete
				if (minu > min) {
					minu -= min;
					path.put(-2, minu);
					deq.addLast(path);
					if (minu < curMin) {
						curMin = minu;
					}
				} else if (minu == min) {
					HashMap<Integer, Integer> map = diGraph.get(lastPoint);
					Iterator<Entry<Integer, Integer>> it = map.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Integer, Integer> entry = it.next();
						Integer next = entry.getKey();
						int weight = entry.getValue();
						if (!banned.contains(next) && !path.containsKey(next)&&!next.equals(endPoint)) {
							HashMap<Integer, Integer> nextPath = new HashMap<>(path);
							nextPath.put(lastPoint, next);
							nextPath.put(-1, next);
							nextPath.put(-2, weight);
							if (weight < curMin) {
								curMin = weight;
							}
							deq.addLast(nextPath);
						}
					}
				}
			}
			min=curMin;
		}
		return false;
	}
	
	private HashMap<Integer,Integer> findNearest(Integer cur, HashSet<Integer> set,HashSet<Integer> banned){
				
		ArrayDeque<HashMap<Integer,Integer>> deq=new ArrayDeque<>();
		//决定用哈希表了,是这样,-1对应的value代表当前最后一个点,-2对应的value代表到当前最后一个点还差的权值
		HashMap<Integer, Integer> path=new HashMap<>();
		
		//初始化路径
		path.put(-1, cur);
		path.put(-2, 0);
		deq.addLast(path);

		int min=0;

		//开始深度优先
		int size=0;
		while((size=deq.size())>0){
			int curMin=0x7fffffff;
			for (int i = 0; i < size; i++) {
				path=deq.removeFirst();
				Integer lastPoint=path.get(-1);
				int minu=path.get(-2);
				if(set.contains(lastPoint)&&minu==min&&!lastPoint.equals(cur)){
					return path;
				}
				assert(minu>=min);		//TODO rememeber to delete
				if(minu>min){
					minu-=min;
					path.put(-2, minu);
					deq.addLast(path);
					if(minu<curMin){
						curMin=minu;
					}
				} else if(minu==min){
					HashMap<Integer, Integer> map = diGraph.get(lastPoint);
					Iterator<Entry<Integer, Integer>> it = map.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Integer, Integer> entry = it.next();
						Integer next = entry.getKey();
						int weight = entry.getValue();
						if (!banned.contains(next)&&!path.containsKey(next)) {
							HashMap<Integer,Integer> nextPath=new HashMap<>(path);
							nextPath.put(lastPoint, next);
							nextPath.put(-1, next);
							nextPath.put(-2, weight);
							if(weight<curMin){
								curMin=weight;
							}
						}
					}
				}
			}
		}
		return null;
	}

	
	//从这里分开,前面的是一种算法,当然initialize函数和trimAndSimpleJudge函数还是用得到的
	//用深度优先算法找到一条路径
	//两个优先
	//优先1:优先选择v'中未经过的点
	//优先2:优先选择权重小的点
	//这样的话需要对原本的initialize函数重新做处理,数据结构也不一样了
	//trimAndSimpleJudge函数也不能用了
	//算了,重来
	//需要加的数据结构如下,第一列索引代表当前点,第二列代表当前点的下面各个点,并按照v'优先,权重优先的顺序排列
	//这样看来, 不用再重载initialize函数和trimAndSimpleJudge函数了,只是需要在trimAndSimpleJudge函数的后面加上对diGraph2的初始化
	private String goTwo(String graphContent, String condition){
		initialize(graphContent,condition);
		if(!trimAndSimpleJudge()){
			return "NA";
		}else{
			initializeGraph2();
			HashMap<Integer,Integer> path=findTwo();
			if(path==null){
				return "NA";
			}else{
				Integer tmp=startPoint;
				StringBuilder sb=new StringBuilder();
				while(!tmp.equals(endPoint)){
					Integer next=path.get(tmp);
					sb.append(dict.get(tmp).get(next));
					sb.append('|');
					tmp=next;
				}
				sb.deleteCharAt(sb.length()-1);
				return sb.toString();
			}
		}
	}
	
	private HashMap<Integer,Integer> findTwo(){
		HashMap<Integer,Integer> path=new HashMap<Integer,Integer>();
		if(dfsTwo(path,startPoint,new HashSet<Integer>(points),0)){
			return path;
		}else{
			return null;
		}
	}
	
	//多点开花好像不行,那从中间开始或许可以呢
	//TODO 
	
	HashMap<Integer,ArrayList<Integer>> diGraph2=new HashMap<>();
	//原来有从自己到自己的线啊, 没考虑进去
	//这个最多支撑50个点,我想试着扩展到100个
	private boolean dfsTwo(HashMap<Integer,Integer> path, Integer cur, HashSet<Integer> vUnvisited, int count){
		
		if(cur.equals(endPoint)){
			return false;
		}
		if(vUnvisited.isEmpty()){
			if(diGraph2.get(cur).get(0).equals(endPoint)){
				path.put(cur, endPoint);
				return true;
			}
		}
		ArrayList<Integer> curMap=diGraph2.get(cur);
		Iterator<Integer> it=curMap.iterator();
		while(it.hasNext()){
			Integer next=it.next();
			if(path.containsKey(next)){
				continue;
			}  
			path.put(cur, next);
			if(points.contains(next)){
				vUnvisited.remove(next);
			}
			if(dfsTwo(path,next,vUnvisited,count+1)){
				return true;
			}
			path.remove(cur);
			if(points.contains(next)){
				vUnvisited.add(next);
			}
		}
		return false;
	}
	
	//初始化diGraph2
	private void initializeGraph2(){
		Iterator<Entry<Integer,HashMap<Integer,Integer>>> it=diGraph.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer,HashMap<Integer,Integer>> entry=it.next();
			Integer key=entry.getKey();
			final HashMap<Integer,Integer> value=entry.getValue();
			ArrayList<Integer> list=new ArrayList<>();
			Iterator<Entry<Integer,Integer>> itt=value.entrySet().iterator();
			while(itt.hasNext()){
				Entry<Integer,Integer> entryy=itt.next();
				list.add(entryy.getKey());
			}
			final Integer endPointCopy=endPoint;
			Collections.sort(list, new Comparator<Integer>(){

				@Override
				public int compare(Integer arg0, Integer arg1) {
					if(arg0.equals(endPointCopy)&&arg1.equals(endPointCopy)){
						return 0;
					}else if(arg0.equals(endPointCopy)){
						return -1;
					}else if(arg1.equals(endPointCopy)){
						return 1;
					}
					boolean b0=points.contains(arg0);
					boolean b1=points.contains(arg1);
					if(b0&&!b1){
						return 1;
					}
					if(!b0&&b1){
						return -1;
					}
					Integer weight0=value.get(arg0);
					Integer weight1=value.get(arg1);
					if(weight0>weight1){
						return -1;
					}else if(weight0==weight1){
						return 0;
					}else{
						return 1;
					}
				}
				
			});
			diGraph2.put(key, list);
		}
	}
	
}