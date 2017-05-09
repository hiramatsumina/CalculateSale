package jp.alhinc.hiramatsu_mina.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class CalculateSales {
	public static void main(String[] args){

		HashMap<String, String> branchNameMap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String, Long>();
		HashMap<String, String> commodityNameMap = new HashMap<String, String>();
		HashMap<String, Long> commoditySaleMap = new HashMap<String, Long>();
		BufferedReader br = null;

		//コマンドライン引数が1つか確認
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//1.支店定義ファイル読込（メソッド）
		if(!printIn(args[0], "branch.lst", branchNameMap, branchSaleMap, "支店", "^\\d{3}$")){
			return;
		}
		//2.商品定義ファイル読込（メソッド）
		if(!printIn(args[0], "commodity.lst", commodityNameMap, commoditySaleMap, "商品", "^\\w{8}$")){
			return;
		}


		//3.集計
		//指定ファイル抽出
		File filesearch = new File(args[0] /*←"C:\\Users\\hiramatsu.mina\\課題"*/);
		String totalfiles[] = filesearch.list();

		ArrayList<String> totals = new ArrayList<String>();

		for(int i = 0; i < totalfiles.length; i++){ //この中では指定のファイルを取り出すだけ
			String tf = totalfiles[i];
			File filetf = new File(args[0],tf); //ファイルに変換し直す、ディレクトリから
			if(tf.matches("^\\d{8}.rcd$") && filetf.isFile()){ //条件に合うものを取り出してListに保持
				totals.add(tf); //ArrayListで保持
			//System.out.println("(抽出リスト)" + totals.get(i)); //確認用
			}
		}

		//連番判定
			//ファイルの最大値
		String totalsmax = Collections.max(totals);
		int max = Integer.parseInt(totalsmax.replace(".rcd", ""));
			//ファイルの最小値
		String totalsmin = Collections.min(totals);
		int min = Integer.parseInt(totalsmin.replace(".rcd", ""));

		if(max - min + 1 != totals.size()){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		FileReader fr;
		String totalbranch; //支店コード
		String totalcommodity; //商品コード
		String totalamount; //金額
		String totalnull; //null
		Long branchtotal; //支店別合計
		Long commoditytotal; //商品別合計

		//売上ファイル読込
		for(int i = 0; i < totals.size(); i++){ //参照しているファイルの要素数で指定
			try{
				File fileread = new File(args[0],totals.get(i)); //ファイルの指定、ディレクトリから
				fr = new FileReader(fileread);
				br = new BufferedReader(fr);

				//（支店コード）
				if((totalbranch = br.readLine()) == null){ //改行でわける、1行ごとに
					System.out.println(totals.get(i) + "のフォーマットが不正です");
					return;
				}
				if(!branchSaleMap.containsKey(totalbranch)){ //抽出した要素とmapの要素が一致するか確認
					System.out.println(totals.get(i) + "の支店コードが不正です");
					return;
				}

				//（商品コード）
				if((totalcommodity = br.readLine()) == null){
					System.out.println(totals.get(i) + "のフォーマットが不正です");
					return;
				}
				if(!commoditySaleMap.containsKey(totalcommodity)){
					System.out.println(totals.get(i) + "の商品コードが不正です");
					return;
				}

				//（金額）
				if((totalamount = br.readLine()) == null || !totalamount.matches("\\d{1,10}")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				//4行目に記入があればエラーを返す
				if((totalnull = br.readLine()) != null){
					System.out.println(totals.get(i) + "のフォーマットが不正です");
					return;
				}

				//加算
				Long amount = Long.parseLong(totalamount);
				branchtotal = amount + branchSaleMap.get(totalbranch);//支店コードで加算
				commoditytotal = amount + commoditySaleMap.get(totalcommodity); //商品コードで加算

				if(branchtotal.toString().length() >= 11 || commoditytotal.toString().length() >= 11){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchSaleMap.put(totalbranch, branchtotal); //Mapに上書き
				commoditySaleMap.put(totalcommodity, commoditytotal); //Mapに上書き

			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				try {
					if(br != null)
						br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}


		//4.集計結果出力（メソッドにて）
		if(!printOut(args[0], "branch.out", branchNameMap, branchSaleMap)){
			return;
		}
		if(!printOut(args[0], "commodity.out", commodityNameMap, commoditySaleMap)){
			return;
		}
	}


	//1.2.定義ファイル読込メソッド
	//引数にディレクトリ、ファイル名、Mapの名前、Mapの金額、ファイル定義名、正規表現での桁数
		public static boolean printIn(String dirPath, String fileName, HashMap<String, String>names, HashMap<String, Long>sales, String definefile, String coad){
			File dirin = new File(dirPath, fileName); //ファイルの指定、ディレクトリから
			if(!dirin.exists()){ //ファイルの存在確認
				System.out.println(definefile + "定義ファイルが存在しません");
				return false;
			}

			BufferedReader br = null;
			String dirinS;

			try{
				FileReader fr = new FileReader(dirin);
				br = new BufferedReader(fr);
				while ((dirinS = br.readLine()) != null){ //ファイルを1行ずつ読込
					String[] s = dirinS.split(",", 0); //カンマで区切る
					//正規表現で文字列、桁数の確認
					if((s.length == 2) && (s[0].matches(coad)) && (0 != s[1].length())){
						names.put(s[0], s[1]); //[0][1]を対応させる
						sales.put(s[0], (long) 0);
					}else {
						System.out.println(definefile + "定義ファイルのフォーマットが不正です");
						return false;
					}
				}
			} catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return false;
			} finally{ //開けないものは閉じられないので
				try {
					if(br != null)
						br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
			return true;
		}


	//4.集計結果出力メソッド
	//引数にディレクトリ、ファイル名、Mapの名前、Mapの金額
	public static boolean printOut(String dirPath, String fileName, HashMap<String, String>names, HashMap<String, Long>sales){
		BufferedWriter bw = null;
		File dirout = new File(dirPath, fileName);

		try{ //降順にソート
			FileWriter fw = new FileWriter(dirout);
			bw = new BufferedWriter(fw);
			List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(sales.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Long>>(){
				public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

			for(Entry<String, Long> s : entries){ //ファイルに出力
				bw.write(s.getKey() + "," + names.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}
		} catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally{
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
}

