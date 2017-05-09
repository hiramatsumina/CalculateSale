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
		//コマンドライン引数が1つか確認
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		HashMap<String, String> branchNameMap = new HashMap<>();
		HashMap<String, Long> branchSaleMap = new HashMap<>();
		HashMap<String, String> commodityNameMap = new HashMap<>();
		HashMap<String, Long> commoditySaleMap = new HashMap<>();

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
		File fileSearch = new File(args[0]);
		String saleFiles[] = fileSearch.list();
		ArrayList<String> matchSaleFile = new ArrayList<String>();

		for(int i = 0; i < saleFiles.length; i++){ //この中では指定のファイルを取り出すだけ
			String sf = saleFiles[i];
			File fileSf = new File(args[0], sf); //ファイルに変換し直す、ディレクトリから
			if(fileSf.isFile() && sf.matches("^\\d{8}.rcd$")){ //ファイルかどうか→数字8桁のrcdか
				matchSaleFile.add(sf); //ArrayListで保持
			}
		}

		//連番判定
			//ファイルの最大値
		String totalsMax = Collections.max(matchSaleFile);
		int max = Integer.parseInt(totalsMax.replace(".rcd", ""));
			//ファイルの最小値
		String totalsMin = Collections.min(matchSaleFile);
		int min = Integer.parseInt(totalsMin.replace(".rcd", ""));

		if(max - min + 1 != matchSaleFile.size()){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		BufferedReader br = null;

		//売上ファイル読込
		for(int i = 0; i < matchSaleFile.size(); i++){ //参照しているファイルの要素数で指定
			try{
				FileReader fr;
				String totalBranch; //支店コード
				String totalCommodity; //商品コード
				String totalAmount; //金額
				String totalNull; //null
				Long branchTotal; //支店別合計
				Long commodityTotal; //商品別合計

				File fileRead = new File(args[0], matchSaleFile.get(i)); //ファイルの指定、ディレクトリから
				fr = new FileReader(fileRead);
				br = new BufferedReader(fr);

				//各行があるか  3行か→要素の一致
				//（支店コード）
				if((totalBranch = br.readLine()) == null){ //改行でわける、1行ごとに
					System.out.println(matchSaleFile.get(i) + "のフォーマットが不正です");
					return;
				}
				//（商品コード）
				if((totalCommodity = br.readLine()) == null){
					System.out.println(matchSaleFile.get(i) + "のフォーマットが不正です");
					return;
				}
				//（金額）
				if((totalAmount = br.readLine()) == null){
					System.out.println(matchSaleFile.get(i) + "のフォーマットが不正です");
					return;
				}
				//4行目に記入があればエラーを返す
				if((totalNull = br.readLine()) != null){
					System.out.println(matchSaleFile.get(i) + "のフォーマットが不正です");
					return;
				}

				//抽出した要素とmapの要素が一致するか確認
				//（支店コード）
				if(!branchSaleMap.containsKey(totalBranch)){
					System.out.println(matchSaleFile.get(i) + "の支店コードが不正です");
					return;
				}
				//（商品コード）
				if(!commoditySaleMap.containsKey(totalCommodity)){
					System.out.println(matchSaleFile.get(i) + "の商品コードが不正です");
					return;
				}
				//（金額）
				if(!totalAmount.matches("\\d{1,10}")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				//加算
				Long amount = Long.parseLong(totalAmount);
				branchTotal = amount + branchSaleMap.get(totalBranch);//支店コードで加算
				commodityTotal = amount + commoditySaleMap.get(totalCommodity); //商品コードで加算
				if(branchTotal.toString().length() >= 11 || commodityTotal.toString().length() >= 11){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				//Mapに上書き
				branchSaleMap.put(totalBranch, branchTotal);
				commoditySaleMap.put(totalCommodity, commodityTotal);

			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				try {
					if(br != null)	br.close();
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
	public static boolean printIn(String dirPath, String fileName, HashMap<String, String>names, HashMap<String, Long>sales, String defineFile, String coad){
		File dirIn = new File(dirPath, fileName); //ファイルの指定、ディレクトリから
		if(!dirIn.exists()){ //ファイルの存在確認
			System.out.println(defineFile + "定義ファイルが存在しません");
			return false;
		}

		BufferedReader br = null;
		String dirInS;

		try{
			FileReader fr = new FileReader(dirIn);
			br = new BufferedReader(fr);
			while ((dirInS = br.readLine()) != null){ //ファイルを1行ずつ読込
				String[] s = dirInS.split(",", 0); //カンマで区切る
				//正規表現で文字列、桁数の確認
				if((s.length == 2) && (s[0].matches(coad))){
					names.put(s[0], s[1]); //[0][1]を対応させる
					sales.put(s[0], 0L);
				}else {
					System.out.println(defineFile + "定義ファイルのフォーマットが不正です");
					return false;
				}
			}
		} catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally{ //開けないものは閉じられないので
			try {
				if(br != null)	br.close();
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
		File dirOut = new File(dirPath, fileName);

		try{ //降順にソート
			FileWriter fw = new FileWriter(dirOut);
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
				if(bw != null)	bw.close();
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
}

