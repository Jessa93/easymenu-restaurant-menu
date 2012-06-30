package biz.easymenu.easymenung;
import java.io.FileNotFoundException;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import biz.easymenu.easymenung.dbtables.DBRowItem;


public class ItemFragment extends DialogFragment {
	
	private LayoutInflater li;
	private DBRowItem item;
	
	private Button btnOk;
	private String sid = null;
	private NumberPicker np = null;
	
	private View v = null;
	private Emrpc rpc = null;
	private EmPrefs emp  = null; 
	int oldNr = 0;
	
	
	ItemFragment (DBRowItem item){
		this.item = item;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  this.setCancelable(false);
		  rpc = new Emrpc(getActivity());
		  emp = new EmPrefs(getActivity());
		  sid = emp.getSid();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		WindowManager.LayoutParams WMLP = this.getDialog().getWindow().getAttributes();
		WMLP.windowAnimations = R.style.PauseDialogAnimation;
		this.getDialog().getWindow().setAttributes(WMLP);
		
        v = li.inflate(R.layout.itemdialog, container, false);
        
        np = (NumberPicker)v.findViewById(R.id.itemnumpick);
        ((TextView)v.findViewById(R.id.itemLabel)).setText(item.getLabel());
        ((TextView)v.findViewById(R.id.itemDescription)).setText(item.getDescription());
        try {
			((ImageView)v.findViewById(R.id.itemImage)).setImageBitmap(BitmapFactory.decodeStream(
					getActivity().openFileInput(Integer.toString(item.getIdImage())+".img")));
		} catch (FileNotFoundException e) {
			Log.e(EasymenuNGActivity.TAG,"Error image file not found: "+e.getMessage());
		}
        
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        new Thread(new NumberRun()).start();
        
        btnOk = (Button)v.findViewById(R.id.itembtnOK);
        btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					if(oldNr>0 || np.getValue()>0)
						rpc.addToOrder(sid, item.getIdMenulists(), np.getValue());
				}catch(Exception e){
					FragmentManager fm = getActivity().getSupportFragmentManager();
	            	FragmentTransaction ft = fm.beginTransaction();
	            	ErrorFragment f = new ErrorFragment(e.getMessage()); 
	            	Fragment prev = fm.findFragmentByTag("errorDialog");
	                if (prev != null) {
	                    ft.remove(prev);
	                    ft.commit();
	                }
	                f.show(ft, "errorDialog");
				}finally{
					dismiss();
				}
			}
		});
        
        return v;
    }

	 final Handler NumberHandler = new Handler() {
	  	  
	        public void handleMessage(Message msg) {
	        	if(msg.what>-1){
	        		oldNr = msg.what;
	        		np.setValue(msg.what);
	        		v.findViewById(R.id.pBarItem).setVisibility(View.GONE);
		    		}
	        	}
	    };
	    
	 // RUNNABLE
	    private class NumberRun implements Runnable{
	    	
			@Override
			public void run() {
				
				Emrpc rpc = new Emrpc(getActivity());
				
				if (rpc.getItemNumber(sid, item.getIdMenulists())){
					NumberHandler.sendEmptyMessage(Integer.parseInt((String)rpc.getData()));
				}
				else
				{
					NumberHandler.sendEmptyMessage(-1);
				}
			}
	    }
	
}