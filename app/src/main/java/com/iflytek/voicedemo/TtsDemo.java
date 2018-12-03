package com.iflytek.voicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.voicedemo.R;

//导入TXT文件

//package com.campu;



//import java.io.BufferedInputStream;

//import java.io.BufferedReader;

//import java.io.File;

//import java.io.FileInputStream;

//import java.io.InputStreamReader;

//import java.io.Reader;





public class TtsDemo extends Activity implements OnClickListener {
//	private Button button;
//	private Button button2;

	private static String TAG = "Iat_TtsDemo";
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog iatDialog;
	// 听写结果内容
	private EditText mResultText;

	private SpeechSynthesizer mTts;
	//缓冲进度
	private int mPercentForBuffering = 0;
	//播放进度
	private int mPercentForPlaying = 0;

	private Toast mToast;
	private SharedPreferences mSharedPreferences;

	@SuppressLint("ShowToast")

	/**

	 * 功能：Java读取txt文件的内容

	 * 步骤：1：先获得文件句柄

	 * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取

	 * 3：读取到输入流后，需要读取生成字节流

	 * 4：一行一行的输出。readline()。

	 * 备注：需要考虑的是异常情况

	 * @param filePath

	 */

	/*public static void readTxtFile(String filePath){

		try {

			String encoding="GBK";

			File file=new File(filePath);

			if(file.isFile() && file.exists()){ //判断文件是否存在

				InputStreamReader read = new InputStreamReader(

						new FileInputStream(file),encoding);//考虑到编码格式

				BufferedReader bufferedReader = new BufferedReader(read);

				String lineTxt = null;

				while((lineTxt = bufferedReader.readLine()) != null){

					System.out.println(lineTxt);

				}

				read.close();

			}else{

				System.out.println("找不到指定的文件");

			}

		} catch (Exception e) {

			System.out.println("读取文件内容出错");

			e.printStackTrace();

		}



	}



	public static void main(String argv[]){

		String filePath = "L:\\Apache\\htdocs\\res\\20121012.txt";

//      "res/";

		readTxtFile(filePath);

	}
	*/


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ttsdemo);
//		button = (Button) this.findViewById(R.id.select_txt);
		initLayout();


		// 初始化识别对象
			mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
			// 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
			iatDialog = new RecognizerDialog(this,mInitListener);

		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

		mSharedPreferences = getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
	}

	/**
	 * 初始化Layout。
	 */
	private void initLayout() {
		findViewById(R.id.tts_play).setOnClickListener(this);
		findViewById(R.id.select_txt).setOnClickListener(this);
		findViewById(R.id.clean).setOnClickListener(this);
		findViewById(R.id.pause).setOnClickListener(this);
		findViewById(R.id.resume).setOnClickListener(this);
		findViewById(R.id.stop).setOnClickListener(this);
		mResultText = ((EditText)findViewById(R.id.tts_text));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
				case R.id.tts_play:
					setParam();    // 开始合成
					break;
			case R.id.clean:
				mResultText.setText(null);
				break;
			case R.id.pause:
				onPause();
				break;
			case R.id.resume:
				onResume();
				break;
			case R.id.stop:
				onstop();
				break;
				case R.id.select_txt:{
					Fileservice fileservice = new Fileservice(TtsDemo.this);
					String msg = fileservice.getInputstream("hh.txt");
					if (msg != null) {
						Toast.makeText(TtsDemo.this, msg, Toast.LENGTH_SHORT)
								.show();
						mResultText.setText(msg);
				}
				break;
				}
			}

	}
	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码："+code);
			}
		}
	};

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener recognizerDialogListener=new RecognizerDialogListener(){
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			mResultText.append(text);
			mResultText.setSelection(mResultText.length());
			setParam();	// 开始合成
			//showTip("完成识别2");//稍后
		}

		@Override
		public void onError(SpeechError arg0) {

		}
	};


	private void showTip(final String str)
	{
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * 参数设置
	 * @param param
	 * @return
	 */
	public void Iat_setParam(){
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		}else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT,lag);
		}
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
	}

	/**
	 * 初期化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("初始化失败,错误码："+code);
        	}
		}
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {


		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			mPercentForBuffering = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			mPercentForPlaying = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if(error == null)
			{
				showTip("播放完成");
			}
			else if(error != null)
			{
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSpeakBegin() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSpeakPaused() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSpeakResumed() {
			// TODO Auto-generated method stub

		}


	};

	/**
	 * 参数设置
	 * @param param
	 * @return
	 */
	private void setParam(){
		//设置合成
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
		//设置发音人 voicer为空默认通过语音+界面指定发音人。
		mTts.setParameter(SpeechConstant.VOICE_NAME,"");

		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE,mSharedPreferences.getString("stream_preference", "1"));//0则为听筒
		int code = mTts.startSpeaking(mResultText.getText().toString(), mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			showTip("未安装离线包");
		}
	}




	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTts.stopSpeaking();
		// 退出时释放连接
		mTts.destroy();
		mIat.cancel();
		mIat.destroy();
	}

	@Override
	protected void onResume() {
		//移动数据统计分析
		super.onResume();
//		FlowerCollector.onResume(this);
//		FlowerCollector.onPageStart("TtsDemo");
		mTts.resumeSpeaking();
	}
	@Override
	protected void onPause() {
		//移动数据统计分析
		super.onPause();
//		FlowerCollector.onPageEnd("TtsDemo");
//		FlowerCollector.onPause(this);
		mTts.pauseSpeaking();
	}
	protected void onstop() {
		//移动数据统计分析
		super.onPause();
		mTts.stopSpeaking();
	}
}
