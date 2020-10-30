import java.util.*;
import java.io.*;

public class CYK {
	
	Map<String, String> gram; // 文法規則(開始記号Sを左辺に含まない)を格納するMap
	Map<String, String> gram_s; // 文法規則(開始記号Sを左辺に含む)を格納するMap
	ArrayList<String[][]> tables; // CYK表を格納する可変長配列
	int length; // 入力単語数
	String[] words; // 入力された単語が格納される配列
	int ambiguity; // 曖昧度 CYK表の個数
	
	// コンストラクタ 文法規則と文を仮引数としてフィールドを初期化
	CYK(String grammer, String sentence) {
		// gram gram_sの初期化
		gram = new HashMap<>();
		gram_s = new HashMap<>();
		String[] rules = grammer.split(";"); // 複数の文法規則を1つずつに分割
		for(String rule : rules) {
			String[] lr = rule.split(":"); // 左辺と右辺に分割
			if(lr[0].equals("S")) // 左辺がSであるならgram_sをそうでないならgramを使用
				gram_s.put(lr[1], lr[0]); // 右辺をキーに，左辺を値として格納
			else
				gram.put(lr[1], lr[0]);
		}
		// words, length, tables, ambiguityの初期化
		sentence = sentence.replace(".",""); // 文末のピリオドの消去
		words = sentence.split(" "); // 空白により1単語ずつに分割
		length = words.length; 
		tables = new ArrayList<>();
		ambiguity = 0;
 	}
	
	// 構文解析を行いすべての場合についてCYK表を完成させる
	void parse() {
		int loop = 0;
		tables.add(new String[length][length]);
		
		// tablesに格納されたすべてのCYK表を解析する
		while(loop<=ambiguity) { 
			String[][] t = tables.get(loop);
			// 対角成分の要素
			for(int i=0; i<length; i++) 
				t[i][i] = gram.get(words[i]);
			// 対角成分以外の要素
			for(int d=1; d<length; d++) {
				for(int i=0; i<length-d; i++){
					int j = i + d;
					if(t[i][j]!=null)
						continue;
					boolean isFound = false; // 既に表を埋めたか否かを示す
					for(int k=i; k<j; k++) {
						if(t[i][k]==null || t[k+1][j]==null)
							continue;
						String[] t_ik = t[i][k].split("\\(");
						String[] t_k1j = t[k+1][j].split("\\(");
						String str = t_ik[0] + " " + t_k1j[0]; // 右辺を示す
						Map<String, String> map;
						if(i==0 && j==length-1) // 表の一番右上のときのみ
							map = gram_s; // 開始記号Sが左辺の文法を用いる
						else
							map = gram;
						// 右辺と合致する文法規則を見つけたとき
						if(map.containsKey(str)) {
							if(isFound) { // 既に表を埋めているとき
								ambiguity++; // 曖昧度=CYK表の数をインクリメント
								String[][] t_new = new String[length][length]; // 新しいCYK表を作成
								for(int l=0; l<length; l++)
									for(int m=0; m<length; m++)
										t_new[l][m] = t[l][m];
								t_new[i][j] = map.get(str) + "(" + t_ik[0] + "," + t_k1j[0] + ")";
								tables.add(new String[length][length]);
								tables.set(ambiguity, t_new); // 新しいCYK表(解析途中)をtablesに追加
							} else {
								t[i][j] = map.get(str) + "(" + t_ik[0] + "," + t_k1j[0] + ")";
								isFound = true;
							}
						}
					}
				}
			}
			tables.set(loop, t); // 完成したCYK表をtablesに追加
			loop++;
		}
	}
	
	// CYK表をすべて表示する
	void printTables() {
		for(String[][] t : tables) {
			for(int j=0; j<length; j++)
				System.out.print("--------------");
			System.out.println();
			for(int i=0; i<length; i++) {
				System.out.print("|");
				for(int j=0; j<length; j++)
					System.out.printf("%13s|", t[i][j]!=null ? t[i][j] : " ");
				System.out.println();
				for(int j=0; j<length; j++)
					System.out.print("--------------");
				System.out.println();
			}
			System.out.println();
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		// 矢印をコロンで，規則間をセミコロンで区切り，文法規則(Chomsky標準形)を1つの文字列として格納する
		String grammer = 
				  "S:NP VP;S:NP V;S:PN VP;S:PN V;"
				+ "NP:PN PP;NP:NP PP;NP:Det N;NP:DA N;NP:Det Noun;NP:DA Noun;"
				+ "DA:Det Adj;"
				+ "N:Noun N;N:Noun Noun;"
				+ "VP:V NP;VP:V PP;VP:VP PP;VP:V PN;"
				+ "PP:Prep NP;PP:Prep PN;"
				+ "Noun:telescope;Noun:time;Noun:piano;"
				+ "V:played;V:saw;"
				+ "PN:Mary;PN:Jack;PN:Tom;"
				+ "Det:the;Det:a;"
				+ "Adj:nice;Adj:long;"
				+ "Prep:with;Prep:for";
		// 文をターミナルから入力させる
		System.out.print("Type a sentence : ");
		Scanner sc = new Scanner(System.in);
		String sentence = sc.nextLine();
		// grammer, sentenceを引数としてCYKインスタンスを生成し，解析，表示する
		CYK cyk = new CYK(grammer, sentence);
		cyk.parse();
		cyk.printTables();
	}
	
}
