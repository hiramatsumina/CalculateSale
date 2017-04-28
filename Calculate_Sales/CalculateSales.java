package jp.co.alhinc.hiramatsu_mina.Calculate_Sales;

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
		if(!(args.length == 1)){ //コマンドライン引数が1つか確認
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//1.支店定義ファイル読込
		File branch = new File(args[0],"branch.lst"); //ファイルの指定
		if(!branch.exists()){ //ファイルの存在確認
			System.out.println("支店定義ファイルが存在しません");
			return;
		}
		HashMap<String, String> branchNameMap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String, Long>();
		//while外で使えるようにする
		BufferedReader br = null;
		//try外で使えるようにする
		String branchs;
		try{
			FileReader fr = new FileReader(branch);
			br = new BufferedReader(fr);
			while ((branchs = br.readLine()) != null){ //ファイルを1行ずつ読込
				String[] s1 = branchs.split(",", 0); //カンマで区切る
				//正規表現で文字列、桁数の確認\\d{3}
				if((s1.length == 2) && (s1[0].matches("^\\d{3}$")) && (!(0 == s1[1].length()))){
					branchNameMap.put(s1[0], s1[1]); //[0][1]を対応させる
					branchSaleMap.put(s1[0], (long) 0);
					//System.out.println("(Nameの値)" + branchNameMap.get(s1[0])); //確認用
					//System.out.println("(Saleの値)" + branchSaleMap.get(s1[0])); //確認用
				}else {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
			}
			//System.out.println("(Nameのキーと値)" + branchNameMap.entrySet()); //確認用
			//System.out.println("(Saleのキーと値)" + branchSaleMap.entrySet()); //確認用
		} catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
		} finally{ //開けないものは閉じられないので
			try {
				if(br != null)
				br.close();
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}


		//2.商品定義ファイル読込
		File commodity = new File(args[0],"commodity.lst");
		if(!commodity.exists()){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}
		HashMap<String, String> commodityNameMap = new HashMap<String, String>();
		HashMap<String, Long> commoditySaleMap = new HashMap<String, Long>();
		try{
			FileReader fr = new FileReader(commodity);
			br = new BufferedReader(fr);
			String commoditys;
			while((commoditys = br.readLine()) != null){
				String[] s2 = commoditys.split(",",0);
				if((s2.length == 2) && (s2[0].matches("^\\w{8}$")) && (!(0 == s2[1].length()))){
					commodityNameMap.put(s2[0],s2[1]);
					commoditySaleMap.put(s2[0], (long) 0);
					//System.out.println("(Nameの値)" + commodityNameMap.get(s2[0])); //確認用
					//System.out.println("(Saleの値)" + commoditySaleMap.get(s2[0])); //確認用
				}else {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
			}
			//System.out.println("(Nameのキーと値)" + commodityNameMap.entrySet()); //確認用
			//System.out.println("(Saleのキーと値)" + commoditySaleMap.entrySet()); //確認用
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
		} finally{
			try {
				if(br != null)
				br.close();
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}


		//3.集計
		File filesearch = new File(args[0] /*←"C:\\Users\\hiramatsu.mina\\課題"*/);
		String totalfiles[] = filesearch.list();

		ArrayList<String> totals = new ArrayList<String>();

		for(int i = 0; i < totalfiles.length; i++){ //この中では指定のファイルを取り出すだけ
			String tf = totalfiles[i];
			if(tf.matches("^\\d{8}.rcd$")){ //条件に合うものを取り出してListに保持
			totals.add(tf); //ArrayListで保持
			//System.out.println("(抽出リスト)" + totals.get(i)); //確認用
			}
		}

			//連番判定
		//ファイルの最大値
		String totalsmax = Collections.max(totals);
		//System.out.println("(最大値ファイル)" + totalsmax); //確認用
		int max = Integer.parseInt(totalsmax.replace(".rcd", ""));
		//System.out.println("(最大値)" + max); //確認用

		//ファイルの最小値
		String totalsmin = Collections.min(totals);
		//System.out.println("(最小値ファイル)" + totalsmin); //確認用
		int min = Integer.parseInt(totalsmin.replace(".rcd", ""));
		//System.out.println("(最小値)" + min); //確認用

		//System.out.println("(リスト要素数)" + totals.size()); //確認用
		//System.out.println(max - min + 1 == totals.size()); //確認用

		if(!(max - min + 1 == totals.size())){
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

		for(int i = 0; i < totals.size(); i++){ //参照しているファイルの要素数で指定
			try{
				File fileread = new File(args[0],totals.get(i)); //ファイルの指定、ディレクトから
				fr = new FileReader(fileread);
				br = new BufferedReader(fr);

				//（支店コード）
				if((totalbranch = br.readLine()) == null || !branchSaleMap.containsKey(totalbranch)){
					//改行でわける、1行ごとに
					//抽出した要素とmapの要素が一致するか確認
					System.out.println(totals.get(i) + "の支店コードが不正です");
					return;
				}

				//（商品コード）
				if((totalcommodity = br.readLine()) == null || !commoditySaleMap.containsKey(totalcommodity)){
					System.out.println(totals.get(i) + "の商品コードが不正です");
					return;
				}

				//（金額）
				if((totalamount = br.readLine()) == null || totalamount.matches("\\D")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				//System.out.println("(ファイル金額)" + totalamount); //確認用

				//4行目に記入があればエラーを返す
				if((totalnull = br.readLine()) != null){
					System.out.println(totals.get(i) + "のフォーマットが不正です");
					return;
				}

				//加算
				Long amount = Long.parseLong(totalamount);
				branchtotal = amount + branchSaleMap.get(totalbranch); //支店コードで加算
				branchSaleMap.put(totalbranch, branchtotal); //Mapに上書き
				commoditytotal = amount + commoditySaleMap.get(totalcommodity); //商品コードで加算
				commoditySaleMap.put(totalcommodity, commoditytotal); //Mapに上書き


				if(!(branchtotal.toString().matches("^\\d{1,10}$") || commoditytotal.toString().matches("^\\d{1,10}$"))){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				//確認用
//				System.out.println("(ファイル支店コード)" + totalbranch);
//				System.out.println("(ファイル商品コード)" + totalcommodity);
//				System.out.println("(支店別金額)" + branchtotal);
//				System.out.println("(商品別金額)" + commoditytotal);

				br.close();
				}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
			}
		}




		//4.集計結果出力
		File branchout = new File(args[0],"branch.out"); //支店別ファイルの作成

		try{ //降順にソート
			FileWriter fw = new FileWriter(branchout,true);
			BufferedWriter bw = new BufferedWriter(fw);
			List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(branchSaleMap.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Long>>(){
				public int compare(
						Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

			for(Entry<String, Long> s : entries){
				//System.out.println("(支店別金額降順)" + s.getKey() + "," + branchNameMap.get(s.getKey()) + "," + s.getValue()); //確認用
				bw.write(s.getKey() + "," + branchNameMap.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}
			bw.close();
		} catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
		}

		File commodityout = new File(args[0], "commodity.out"); //商品別ファイルの作成

		try{
			FileWriter fw = new FileWriter(commodityout,true);
			BufferedWriter bw = new BufferedWriter(fw);
			List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(commoditySaleMap.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Long>>(){
				public int compare(
						Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

			for(Entry<String, Long> s : entries){
				//System.out.println("(商品別金額降順)" + s.getKey() + "," + commodityNameMap.get(s.getKey()) + "," + s.getValue()); //確認用
				bw.write(s.getKey() + "," + commodityNameMap.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}
			bw.close();
		} catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
		}
	}


}

