package com.darwin.sample.stickerscode.utils;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.darwin.sample.R;


public class StickerInfoInputDialogue extends DialogFragment {

    private EditText mEditText;

    public StickerInfoInputDialogue() {
            super(R.layout.add_pack_info_dialog);
    }

//    public StickerInfoInputDialogue() {
//        // Empty constructor is required for DialogFragment
//        // Make sure not to add arguments to the constructor
//        // Use `newInstance` instead as shown below
//    }

//    public static StickerInfoInputDialogue newInstance(String title) {
//        StickerInfoInputDialogue frag = new StickerInfoInputDialogue();
//        Bundle args = new Bundle();
//        args.putString("title", title);
//        frag.setArguments(args);
//        return frag;
//    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.add_pack_info_dialog, container);
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        // Get field from view
//        mEditText = (EditText) view.findViewById(R.id.pack_name);
//        // Fetch arguments from bundle and set title
////        String title = getArguments().getString("title", "Enter Name");
////        getDialog().setTitle(title);
//        // Show soft keyboard automatically and request focus to field
//        mEditText.requestFocus();
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        return new AlertDialog.Builder(requireContext())
                .setMessage("Enter the name for your new pack.")
                .setPositiveButton("OK", (dialog, which) -> {
//                    try {
//                        StickerAndPackHandler.addBitmap(
//                                getContext(),
//                                bmp,
//                                mEditText.getText().toString()
//                        );
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Log.d("StickerAddLog", "onCreateDialog: "+e.getMessage());
//                    }
                } )
                .create();
    }

    //    EditText name;
//    Context ctxt;
//    AppCompatActivity activity;
//    String chosen_name;
//    StickerAndPackHandler adder;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
////        return super.onCreateView(inflater, container, savedInstanceState);
//        return inflater.inflate(R.layout.add_pack_info_dialog,container, false);
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        name = activity.findViewById(R.id.pack_name);
//        return new AlertDialog.Builder(requireContext())
//                .setMessage("Enter the name for your new pack.")
//                .setPositiveButton("OK", (dialog, which) -> {
////                    try {
////                        StickerAndPackHandler.addBitmap(
////                                ctxt,
////                                adder.bmp,
////                                name.getText().toString()
////                        );
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                        Log.d("StickerAddLog", "onCreateDialog: "+e.getMessage());
////                    }
//                } )
//                .create();
//    }
//
//
//    public void setName(String name){
//        chosen_name = name;
//    }
//
//    public void setAdder(StickerAndPackHandler adder){
//        this.adder = adder;
//    }
//
//    public void setActivity(AppCompatActivity activity){
//        this.activity = activity;
//    }
//
//    public static String TAG = "PurchaseConfirmationDialog";
//
//    public void setContext(Context ctxt) {
//        this.ctxt = ctxt;
//    }
}