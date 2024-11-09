package pers.beapoe.menu_merchant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CreateStore extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_store);
    }

    public void BindOnClick(View v){
        final String TAG = "CreateStore:BindOnClick(...)";

        SharedPreferences sp = getSharedPreferences("storeInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        EditText serverAddress_EditText = findViewById(R.id.ServerAddress);
        EditText name_EditText = findViewById(R.id.Name);
        EditText address_EditText = findViewById(R.id.Address);
        EditText bindPwd_EditText = findViewById(R.id.BindPassword);
        EditText phoneNum_EditText = findViewById(R.id.PhoneNum);
        ArrayList<EditText> texts = new ArrayList<>();
        texts.add(serverAddress_EditText);
        texts.add(name_EditText);
        texts.add(address_EditText);
        texts.add(bindPwd_EditText);
        texts.add(phoneNum_EditText);
        ArrayList<String> info = new ArrayList<>();
        for(EditText text:texts) info.add(text.getText().toString());
        boolean EmptyInfo = false;
        for(String information:info)
            if (information.isEmpty()) {
                EmptyInfo = true;
                break;
            }
        if(!EmptyInfo){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 10000);
            } else {
                Toast.makeText(this, "网络权限已授权", Toast.LENGTH_SHORT).show();
            }
            editor.putString("serverAddress",info.get(0));
            editor.putString("storeName",info.get(1));
            editor.putString("storeAddress",info.get(2));
            editor.putString("storeBindPwd",info.get(3));
            editor.putString("storePhoneNum",info.get(4));
            editor.apply();
            for(int i=1;i< info.size();i++) info.set(i, Base64Utils.encode(info.get(i),true));
            try {
                URL url = new URL("http://"+info.get(0));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(false);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(true);
                connection.setConnectTimeout(3000);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                connection.setRequestProperty("Connection", "Keep-Alive");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.connect();
                            String params = "name="+info.get(1)+"&address="+info.get(2)+"&bindPassword="+info.get(3)+"&PhoneNum="+info.get(4);
                            OutputStream ops = connection.getOutputStream();
                            ops.write(params.getBytes());
                            ops.flush();
                            ops.close();
                            if(connection.getResponseCode() != 200){
                                Log.e(TAG,"Server returns wrong response code");
                                Toast.makeText(CreateStore.this,"网络错误，请稍后重试",Toast.LENGTH_SHORT).show();
                                Process.killProcess(Process.myPid());
                                System.exit(-1);
                            }
                            connection.disconnect();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                Toast.makeText(this,"成功绑定",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final String TAG = "CreateStore:onRequestPermissionsResult(...)";
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try{
            ArrayList<String> requestList=new ArrayList<>();//允许询问列表
            ArrayList<String> banList=new ArrayList<>();//禁止列表
            for(int i=0;i<permissions.length;i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG,"【"+permissions[i]+"】权限授权成功");
                }
                else{
                    //判断是否允许重新申请该权限
                    boolean nRet=ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i]);
                    Log.i(TAG,"shouldShowRequestPermissionRationale nRet="+nRet);
                    if(nRet){//允许重新申请
                        requestList.add(permissions[i]);
                    }
                    else{//禁止申请
                        banList.add(permissions[i]);
                    }
                }
            }

            //优先对禁止列表进行判断
            if(!banList.isEmpty()){//告知该权限作用，要求手动授予权限
                showFinishedDialog();
            }
            else if(!requestList.isEmpty()){//告知权限的作用，并重新申请
                showTipDialog(requestList);
            }
            else{
                Toast.makeText(this,"权限授权成功",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Log.e(TAG,"权限申请回调中发生异常");
            android.os.Process.killProcess(Process.myPid());
            System.exit(-1);
            Toast.makeText(this,"权限申请回调中发生异常",Toast.LENGTH_SHORT).show();
        }
    }

    public void showFinishedDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("请前往设置中打开相关权限，否则功能无法正常运行！")
                .setPositiveButton("确定", (dialog1, which) -> {
                    // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                    finish();
                })
                .create();
        dialog.show();
    }

    public void showTipDialog(ArrayList<String> pmList){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("【"+pmList.toString()+"】权限为应用必要权限，请授权")
                .setPositiveButton("确定", (dialog1, which) -> {
                    String[] sList=pmList.toArray(new String[0]);
                    //重新申请该权限
                    ActivityCompat.requestPermissions(CreateStore.this,sList,10000);
                })
                .create();
        dialog.show();
    }
}
