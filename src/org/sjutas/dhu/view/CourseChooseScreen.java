package org.sjutas.dhu.view;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetClient;
import org.sjutas.dhu.net.NetConstant;

public class CourseChooseScreen extends WindowActivity implements
		OnClickListener {
	private class XuanKeInfoListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mXuanKeInfoListResults.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.xuanke_info_list_item_layout, null);
			}
			TextView classname = (TextView) convertView
					.findViewById(R.id.class_name);
			classname.setText(mXuanKeInfoListResults.get(position)[0]);
			TextView score = (TextView) convertView.findViewById(R.id.score);
			score.setText(mXuanKeInfoListResults.get(position)[1]);
			TextView address = (TextView) convertView
					.findViewById(R.id.address);
			address.setText(mXuanKeInfoListResults.get(position)[2]);
			TextView teacher = (TextView) convertView
					.findViewById(R.id.teacher);
			teacher.setText(mXuanKeInfoListResults.get(position)[3]);
			System.out.println("mXuanKeInfoListResults.get(position)[3]:"
					+ mXuanKeInfoListResults.get(position)[3]);
			TextView term = (TextView) convertView.findViewById(R.id.term);
			term.setText(mXuanKeInfoListResults.get(position)[4]);
			TextView class_type = (TextView) convertView
					.findViewById(R.id.class_type);
			class_type.setText(mXuanKeInfoListResults.get(position)[5]);
			return convertView;
		}

	}

	private ArrayList<String[]> hide_params = new ArrayList<String[]>();

	private int mCurSelectYearIndex, mCurSelectTermIndex;

	private NetClient mNetClient = Globe.sNetClient;

	private ListView mResultListView;

	private ArrayAdapter<String> mTermSpinnerAdapter;

	private String[] mTermStrs;

	private TextView mUserInfoTextView, mXueFenInfoOne, mXueFenInfoTwo,
			mXueFenInfoThree;

	private XuanKeInfoListAdapter mXuanKeInfoListAdapter;

	private ArrayList<String[]> mXuanKeInfoListResults = new ArrayList<String[]>();

	private Button mXuanKeSearchBtn;

	private Spinner mYearSpinner, mTermSpinner;

	private String[] mYearStrs;

	private ArrayAdapter<String> mYesrSpinnerAdapter;

	private ImageView mImageView;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		// if (myHttpResponse.getPipIndex() == NetConstant.XKCX_HOMEPAGE) {
		// Document doc = myHttpResponse.getData();
		// // System.out.println("查询首页:" + doc.toString());
		// Element ele = doc.getElementById("lbXb");
		// mUserInfoTextView.setText(Globe.sName_jw + "  " + Globe.sClassName_jw
		// + "  " + ele.text());
		// mXueFenInfoOne.setText("最低毕业学分要求  公选:"
		// + doc.getElementById("lbGxMax").text() + "  专业选修:"
		// + doc.getElementById("lbZyMax").text());
		// mXueFenInfoTwo.setText("已选公选课通过学分:"
		// + doc.getElementById("lbGxxf").text() + "  已选公选课未通过学分:"
		// + doc.getElementById("lbGxbjgxf").text() + "  公选可选学分:"
		// + doc.getElementById("lbGx").text() + "  本学期公选已选门次:"
		// + doc.getElementById("lbGxCount").text() + "  本学期公选允许最大门次:"
		// + doc.getElementById("lbGxMaxCount").text());
		// mXueFenInfoThree.setText("已选专业选修课通过学分:"
		// + doc.getElementById("lbZyxf").text() + "  已选专业选修课未通过学分:"
		// + doc.getElementById("lbZybjgxf").text() + "  专业选修可选学分:"
		// + doc.getElementById("lbZy").text());
		//
		// ele = doc.getElementById("selYear");
		// Elements eles = ele.getAllElements();
		//
		// mYearStrs = new String[eles.size() - 1];
		// for (int i = 1; i < eles.size(); i++) {
		// mYearStrs[i - 1] = eles.get(i).text();
		// if (eles.get(i).hasAttr("selected")) {
		// mCurSelectYearIndex = i - 1;
		// }
		// }
		// ele = doc.getElementById("selTerm");
		// eles = ele.getAllElements();
		// mTermStrs = new String[eles.size() - 1];
		// for (int i = 1; i < eles.size(); i++) {
		// mTermStrs[i - 1] = eles.get(i).text();
		// if (eles.get(i).hasAttr("selected")) {
		// mCurSelectTermIndex = i - 1;
		// }
		// }
		// mYesrSpinnerAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item, mYearStrs);
		// mYesrSpinnerAdapter
		// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// mYearSpinner.setAdapter(mYesrSpinnerAdapter);
		// mYearSpinner.setSelection(mCurSelectYearIndex);
		// mTermSpinnerAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item, mTermStrs);
		// mTermSpinnerAdapter
		// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// mTermSpinner.setAdapter(mTermSpinnerAdapter);
		// mTermSpinner.setSelection(mCurSelectTermIndex);
		// Elements hidden_inputs = doc.select("input[type=hidden]");
		// for (Element hidden_eles : hidden_inputs) {
		// String[] arr = new String[] { hidden_eles.attr("name"),
		// hidden_eles.attr("value") };
		// hide_params.add(arr);
		// // System.out.println("获得的隐藏参数:" + arr[0] + "\n" + arr[1]);
		// }
		// System.out.println("获取选课查询主页信息成功");
		// } else if (myHttpResponse.getPipIndex() == NetConstant.XKCX_INFO) {
		// mXuanKeInfoListResults.clear();
		// Document doc = myHttpResponse.getData();
		// Elements eles = doc.select("a[target=_blank]");
		// for (Element ele : eles) {
		// String[] str = new String[6];
		// str[0] = ele.text();
		// Element ele_next = ele.parent().parent().nextElementSibling();
		// System.out.println(ele_next);
		// str[1] = ele_next.child(0).text();
		// System.out.println(str[1]);
		// ele_next = ele_next.nextElementSibling();
		// str[2] = ele_next.child(0).text();
		// ele_next = ele_next.nextElementSibling();
		// str[3] = ele_next.child(0).text();
		// ele_next = ele_next.nextElementSibling();
		// str[4] = ele_next.child(0).text();
		// ele_next = ele_next.nextElementSibling();
		// str[5] = ele_next.child(0).text();
		// mXuanKeInfoListResults.add(str);
		// }
		// mXuanKeInfoListAdapter.notifyDataSetChanged();
		// hide_params.clear();
		// Elements hidden_inputs = doc.select("input[type=hidden]");
		// for (Element hidden_eles : hidden_inputs) {
		// String[] arr = new String[] { hidden_eles.attr("name"),
		// hidden_eles.attr("value") };
		// hide_params.add(arr);
		// // System.out.println("获得的隐藏参数:" + arr[0] + "\n" + arr[1]);
		// }
		// // System.out.println(doc.toString());
		// }

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		MyHttpRequest req;
		switch (v.getId()) {
		case R.id.search_btn:
			params.add(new BasicNameValuePair("selTerm", ""
					+ (mTermSpinner.getSelectedItemPosition() + 1)));
			params.add(new BasicNameValuePair("selYear", mYearStrs[mYearSpinner
					.getSelectedItemPosition()]));
			params.add(new BasicNameValuePair("__EVENTTARGET", "btQuery"));
			params.add(new BasicNameValuePair("selXiaoqu", "1"));
			for (String[] arr : hide_params) {
				// System.out.println("插入hiden参数:" + arr[0] + "\n" +
				// arr[1]);
				params.add(new BasicNameValuePair(arr[0], arr[1]));
			}
			req = new MyHttpRequest(NetConstant.TYPE_POST,
					NetConstant.URL_COURSE_CHOOSE, params, true);
			req.setPipIndex(NetConstant.XKCX_INFO);
			mNetClient.sendRequest(req);
			break;
		// case R.id.all_score_btn:
		// params.add(new BasicNameValuePair("selTerm", ""
		// + (mTermSpinner.getSelectedItemPosition() + 1)));
		// params.add(new BasicNameValuePair("selYear",
		// mYearStrs[mYearSpinner
		// .getSelectedItemPosition()]));
		// params.add(new BasicNameValuePair("__EVENTTARGET",
		// mInputStrs.get(1)[0]));
		// for (String[] arr : hide_params) {
		// System.out.println("插入hiden参数:" + arr[0] + "\n" + arr[1]);
		// params.add(new BasicNameValuePair(arr[0], arr[1]));
		// }
		// req = new MyHttpRequest(NetConstant.TYPE_POST,
		// NetConstant.URL_CJCX, params, true);
		// req.setPipIndex(NetConstant.CJCX_SEARCH);
		// mNetClient.sendRequest(req);
		// break;
		// case R.id.ex_search_btn1:
		// params.add(new BasicNameValuePair("selTerm", ""
		// + (mTermSpinner.getSelectedItemPosition() + 1)));
		// params.add(new BasicNameValuePair("selYear",
		// mYearStrs[mYearSpinner
		// .getSelectedItemPosition()]));
		// params.add(new BasicNameValuePair(mInputStrs.get(2)[0],
		// mInputStrs.get(2)[1]));
		// for (String[] arr : hide_params) {
		// System.out.println("插入hiden参数:" + arr[0] + "\n" + arr[1]);
		// params.add(new BasicNameValuePair(arr[0], arr[1]));
		// }
		// req = new MyHttpRequest(NetConstant.TYPE_POST,
		// NetConstant.URL_CJCX, params, true);
		// req.setPipIndex(NetConstant.CJCX_SEARCH);
		// mNetClient.sendRequest(req);
		// break;
		// case R.id.ex_search_btn2:
		// params.add(new BasicNameValuePair("selTerm", ""
		// + (mTermSpinner.getSelectedItemPosition() + 1)));
		// params.add(new BasicNameValuePair("selYear",
		// mYearStrs[mYearSpinner
		// .getSelectedItemPosition()]));
		// params.add(new BasicNameValuePair(mInputStrs.get(3)[0],
		// mInputStrs.get(3)[1]));
		// for (String[] arr : hide_params) {
		// System.out.println("插入hiden参数:" + arr[0] + "\n" + arr[1]);
		// params.add(new BasicNameValuePair(arr[0], arr[1]));
		// }
		// req = new MyHttpRequest(NetConstant.TYPE_POST,
		// NetConstant.URL_CJCX, params, true);
		// req.setPipIndex(NetConstant.CJCX_SEARCH);
		// mNetClient.sendRequest(req);
		// break;

		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.xkcx_screen_layout);

		setSlidingMenu();

		// overridePendingTransition(R.anim.scale_translate,
		// R.anim.my_alpha_action);
		// mUserInfoTextView = (TextView) findViewById(R.id.user_info);
		// mXueFenInfoOne = (TextView) findViewById(R.id.xuefen_info1);
		// mXueFenInfoTwo = (TextView) findViewById(R.id.xuefen_info2);
		// mXueFenInfoThree = (TextView) findViewById(R.id.xuefen_info3);
		// mYearSpinner = (Spinner) findViewById(R.id.year_spinner);
		// mTermSpinner = (Spinner) findViewById(R.id.term_spinner);
		// mXuanKeSearchBtn = (Button) findViewById(R.id.search_btn);
		// mResultListView = (ListView) findViewById(R.id.xuanke_info_list);
		// mXuanKeInfoListAdapter = new XuanKeInfoListAdapter();
		mResultListView.setAdapter(mXuanKeInfoListAdapter);
		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_COURSE_CHOOSE, null, true);
		req.setPipIndex(NetConstant.XKCX_HOMEPAGE);
		mNetClient.sendRequest(req);
		mXuanKeSearchBtn.setOnClickListener(this);

		mImageView = (ImageView) findViewById(R.id.menu_icon_course);
		mImageView.setImageResource(R.drawable.ic_action_location_found);

		findViewById(R.id.menu_course_choose).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						menu.showContent();
					}
				});
	}
}
