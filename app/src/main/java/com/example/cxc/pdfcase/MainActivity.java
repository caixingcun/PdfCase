package com.example.cxc.pdfcase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private PDFView mPdfView;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        findViewById(R.id.btn_general_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        convertPdf();
                    }
                }).start();
            }
        });
        findViewById(R.id.btn_show_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!new File(path, SrcFileName).exists()) {
                    Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
                }
                mPdfView.fromFile(new File(path,SrcFileName))
                        .enableSwipe(true)
                        .enableAnnotationRendering(true)
                        .spacing(10) // in dp
                        .onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                            }
                        })
                        .pageFitPolicy(FitPolicy.BOTH)
                        .load();
            }
        });

        mPdfView = findViewById(R.id.pdf_view);

    }

    private void convertPdf() {
        String timeWater = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        try {
            BaseFont bfChinese = BaseFont.createFont("assets/font/simhei.ttf",
                    BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            convertPDFInfo( timeWater, bfChinese);

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String SrcFileName = "pdfmodel.pdf";

    private void convertPDFInfo( String timeWater, BaseFont bfChinese) throws IOException, DocumentException {
        InputStream open = mContext.getClass().getClassLoader().getResourceAsStream("assets/" +SrcFileName );
        PdfReader reader = new PdfReader(open);
        Log.d("tag",path);
        PdfStamper pdfStamper = new PdfStamper(reader, new FileOutputStream(new File(path, SrcFileName)));
        AcroFields acroFields = pdfStamper.getAcroFields();



        Map<String, String> formData = new HashMap<>();
        formData.put("text1", "我是填充内容");


        for (Iterator it = formData.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            String value = (String) formData.get(key);
            acroFields.setFieldProperty(key, "textfont", bfChinese, null);
            acroFields.setField(key, value);
        }

        //追加图片
//        PdfContentByte overContent19 = pdfStamper.getUnderContent(2);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);



//        Image idFontImg = Image.getInstance(new File("assets/" +"ic_launcher.png" ).getAbsolutePath());
//
//        Rectangle rectangleIdFont;
//        if (idFontImg.getWidth() > idFontImg.getHeight()) {
//            rectangleIdFont = new Rectangle(400, 300);
//        } else {
//            rectangleIdFont = new Rectangle(300, 400);
//        }
//        idFontImg.scaleToFit(rectangleIdFont.getWidth(), rectangleIdFont.getHeight());
//
//        overContent19.addImage(idFontImg);



        addMaterMark(reader, pdfStamper, bfChinese, timeWater);
        pdfStamper.setFormFlattening(true);
        pdfStamper.close();
        reader.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "pdf完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加水印
     *
     * @param reader
     * @param stamper
     * @param bfChinese
     * @param timeWater
     */
    private void addMaterMark(PdfReader reader, PdfStamper stamper, BaseFont bfChinese, String timeWater) {
        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
        int fontSize = 70;
        int rotate = 45;
        BaseColor color = BaseColor.GRAY;
        // 循环对每页插入水印
        for (int i = 1; i < total; i++) {
            // 水印的起始
            content = stamper.getOverContent(i);

            // 开始
            content.beginText();

            // 设置颜色 默认为蓝色1
            content.setColorFill(color);
            // 设置字体及字号
            content.setFontAndSize(bfChinese, fontSize);

            Document document = new Document(reader.getPageSize(1));
            float pageWidth = document.getPageSize().getWidth(); //595
            float pageHeight = document.getPageSize().getHeight();//841
//            content.setTextRise(45);//斜度
            // 设置起始位置
            // content.setTextMatrix(400, 880);
            //  content.setTextMatrix((pageWidth-fontSize*stampStr.length())/2+50,(pageHeight-fontSize*stampStr.length())/2+100);
            // 开始写入水印


            //因为是纯数字 宽度占一半  因为45度 宽度除以 1.414   左右对称  再除以2
            int left = (int) ((pageWidth - fontSize * timeWater.length() / 2 / 1.414) / 2);
            int top = (int) ((pageHeight - fontSize * timeWater.length() / 2 / 1.414) / 2);
            Log.d("tag", "left_" + left);
            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.2f);
            content.setGState(gs);
            content.showTextAligned(Element.ALIGN_LEFT, timeWater, left,
                    top, rotate);

            content.endText();

        }
    }
}
