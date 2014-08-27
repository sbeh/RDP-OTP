package de.sbeh.rdp_otp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	TextView secret;
	Button rescan, genkey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		secret = (TextView) findViewById(R.id.secret);
		rescan = (Button) findViewById(R.id.rescan);
		rescan.setOnClickListener(this);
		genkey = (Button) findViewById(R.id.genkey);
		genkey.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanResult != null) {
			try {
				byte[] encrypted = Base64.decode(scanResult.getContents(),
						Base64.DEFAULT);

				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				
				FileInputStream fis = openFileInput("prvkey");
				byte[] key = new byte[fis.available()];
				fis.read(key);
				fis.close();

				cipher.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key)));
				
				String decrypted = new String(cipher.update(encrypted), "UTF-8")
						+ new String(cipher.doFinal(), "UTF-8");
				secret.setText(decrypted);
			} catch (Exception e) {
				Log.e("", "Decrypting error", e);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v == rescan)
			rescan();
		else if (v == genkey)
			genkey();
	}

	private void genkey() {
		try {
			Key privateKey, publicKey;

			{
				KeyPair kp;
				{
					KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
					kpg.initialize(1024);
					kp = kpg.genKeyPair();
				}
				privateKey = kp.getPrivate();
				publicKey = kp.getPublic();
			}

			try {
				FileOutputStream fos = openFileOutput("prvkey", Context.MODE_PRIVATE);
				fos.write(new PKCS8EncodedKeySpec(privateKey.getEncoded()).getEncoded());
				fos.close();
			} catch(Exception e) {
				Log.e("", "Error while saving private key", e);
			}

			try {
				String pk = Base64.encodeToString(publicKey.getEncoded(),
						Base64.DEFAULT);
				write(Environment.getExternalStorageDirectory() + "/pubkey.pem",
						("-----BEGIN PUBLIC KEY-----\r\n" + pk + "\r\n-----END PUBLIC KEY-----"));
				/*
				 * write(Environment.getExternalStorageDirectory() + "/pubkey",
				 * pk);
				 */
			} catch (Exception e) {
				Log.e("", "Error while saving public key", e);
			}

			try {
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				byte[] encodedBytes = cipher.doFinal("asdf".getBytes("UTF-8"));

				try {
					cipher.init(Cipher.DECRYPT_MODE, privateKey);
					byte[] decodedBytes = cipher.doFinal(encodedBytes);
					if (!new String(decodedBytes, "UTF-8").equals("asdf"))
						finish();
				} catch (Exception e) {
					Log.e("", "RSA decryption error", e);
				}
			} catch (Exception e) {
				Log.e("", "RSA encryption error", e);
			}
		} catch (Exception e) {
			Log.e("", "RSA key pair error");
		}
	}

	private void write(String file, String data) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data.getBytes("UTF-8"));
		fos.flush();
		fos.close();
	}

	private void rescan() {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}
}
