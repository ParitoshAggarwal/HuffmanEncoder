import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class HEncoder extends JFrame implements ActionListener{
	private HashMap<Character, String> encoder = new HashMap<>();
	private HashMap<String, Character> decoder = new HashMap<>();
	
	private File file;
	private JButton btnStart;
	private JButton btnStop;
	private JLabel lblWord;
	private JTextField txtWord;
	
	private static class Node{
		Character data;
		int freq;
		Node left;
		Node right;
		private static final NodeComparator Ctor = new NodeComparator();
		
		private static class NodeComparator implements Comparator<Node>{
			@Override
			public int compare(Node o1, Node o2) {
				return o2.freq - o1.freq;
			}
		}
	}
	
	// 1. freq map
	// 2. prepare the heap from keyset
	// 3. prepare tree - remove two, merge, add it back
	// 4. traverse
	public HEncoder(String feeder){
		// 1. freq map
		HashMap<Character, Integer> fm = new HashMap<>();
		for(int i = 0; i < feeder.length(); i++){
			char ch = feeder.charAt(i);
			
			if(fm.containsKey(ch)){
				fm.put(ch, fm.get(ch) + 1);
			} else {
				fm.put(ch, 1);
			}
		}
		
		// 2. create the heap
		genericHeap<Node> heap = new genericHeap<>(Node.Ctor);
		ArrayList<Character> keys = new ArrayList<>(fm.keySet());
		for(Character key: keys){
			Node node = new Node();
			node.data = key;
			node.freq = fm.get(key);
			
			heap.add(node);
		}
		
		// 3. create the binary tree - remove two, merge, put it back till size is 1
		while(heap.size() != 1){
			Node one = heap.removeH();
			Node two = heap.removeH();
			
			Node merged = new Node();
			merged.freq = one.freq + two.freq;
			merged.left = one;
			merged.right = two;
			
			heap.add(merged);
		}
		
		// 4. traverse the tree
		Node finalNode = heap.removeH();
		traverse(finalNode, "");
	}

	private void traverse(Node node, String osf) {
		// work
		if(node.left == null && node.right == null){
			encoder.put(node.data, osf);
			decoder.put(osf, node.data);
			return;
		}
		
		traverse(node.left, osf + "0");
		traverse(node.right, osf + "1");
	}

	public String compress(String str){
		String rv = "";
		
		for(int i = 0; i < str.length(); i++){
			rv += encoder.get(str.charAt(i));
		}
		
		return rv;
	}
	
	public String decompress(String str){
		String rv = "";
		
		String code = "";
		for(int i = 0; i < str.length(); i++){
			code += str.charAt(i);
			
			if(decoder.containsKey(code)){
				rv += decoder.get(code);
				code = "";
			}
		}
		
		return rv;
	}
	
	public void drawgame(){
		GridLayout layout = new GridLayout(3, 2);
		super.setLayout(layout);
		Font font = new Font("Verdana",2,130);
		
		
		
		btnStart = new JButton("Encode");
		btnStart.setFont(font);
		btnStart.addActionListener(this);
		super.add(btnStart);
		
		btnStop = new JButton("Decode");
		btnStop.setFont(font);
		btnStop.addActionListener(this);
		super.add(btnStop);
		
		
		lblWord = new JLabel("hi");
		lblWord.setFont(font);
		super.add(lblWord);
		
		super.setTitle("Huffman Compressor");
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		super.setExtendedState(MAXIMIZED_BOTH);
		super.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == btnStart){
			getfile();
			try {
				performencoding();
				lblWord.setText("Encoding Done");
			} catch (IOException e1) {}
			
		}else if(e.getSource()== btnStop){
			getfile();
			try {
				performdecoding();
				lblWord.setText("Decoding Done");
			} catch (IOException e1) {}
		}
		
	}
	
	 private void performdecoding() throws IOException {
		 	File foldd=new File(file.toString());
			
			
			File fnewe=new File(file.toString().substring(0,file.toString().length()-11)+"encoded.txt");
			fnewe.createNewFile();
			
			FileWriter fnewe_writer=new FileWriter(fnewe);
			BufferedWriter fnewe_buffer=new BufferedWriter(fnewe_writer);
			
			String str="";
			byte[] arr=Files.readAllBytes(foldd.toPath());
			
			for(int i=0;i<arr.length;i++){
				str+=String.format("%8s", Integer.toBinaryString(arr[i] & 0xFF)).replace(' ', '0');
			}
			
			String result=decompress(str);
			fnewe_buffer.write(result);
			fnewe_buffer.close();
	}

	private void performencoding() throws IOException {
		 	File folde=new File(file.toString());
			FileReader folde_reader=new FileReader(folde);
			BufferedReader folde_bufferR=new BufferedReader(folde_reader);
			
			File fnewd=new File(file.toString().substring(0,file.toString().length()-4)+"decoded.txt");
			fnewd.createNewFile();
			
			
			String str="";
			for(int i=0;i<folde.length();i++){
				str+=(char)folde_bufferR.read();
			}
				
			String result=compress(str);
			
			//start
			byte[] barr = null;
			if(result.length()%8==0){
				barr=new byte[result.length()/8];
			}else{
				barr=new byte[result.length()/8+1];
			}
			
			int counter=0;
			for(int i=0;i<result.length();){
				if(i+8<=result.length())
					barr[counter]=(byte)Integer.parseInt(result.substring(i, i+8),2);
				else{
					barr[counter]=(byte)(Integer.parseInt(makestring(result.substring(i,result.length())),2));
				}
				counter++;
				i+=8;
			}
			
			Files.write(fnewd.toPath(), barr);
			//end 
	}
	 
	private String makestring(String rst){
		String temp="";
		for(int i=0;i < 8-rst.length();i++){
			temp+="0";
		}
		return temp+rst;
	}
	 

	private void getfile() {
	        try{
	            JFileChooser choose=new JFileChooser();
	            choose.showOpenDialog(this);
	            file=choose.getSelectedFile();
	            if(!file.exists())
	            {
	                throw new FileNotFoundException();
	            }
	        }
	        catch(Exception e){}
	    }
	
}
