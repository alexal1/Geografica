package com.alex_aladdin.geografica;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

class ExcelParser {

    private InputStream mInputStream;
    private HSSFWorkbook mWorkbook;
    private Iterator<Row> mRows;

    ExcelParser(Context context) {
        try {
            //Получаем доступ к Excel-файлу в папке assets
            AssetManager am = context.getAssets();
            mInputStream = am.open("dvo.xls");

            //Читаем Excel-файл
            mWorkbook = new HSSFWorkbook(mInputStream);
            HSSFSheet sheet = mWorkbook.getSheetAt(0);
            mRows = sheet.iterator();

            //Пропускаем заголовок
            mRows.next();
        } catch (Exception e) {
            Log.i("Parsing", String.valueOf(e));
        }
    }

    //Метод, возвращающий данные текущей строки в виде отображения HashMap
    HashMap<String, String> getNextMap() {
        if (mRows.hasNext()) {
            Row row = mRows.next();

            Cell name = row.getCell(1);
            Cell x = row.getCell(2);
            Cell y = row.getCell(3);

            Log.i("Parsing", "name = " + name.toString() +
                    ", x = " + x.toString() +
                    ", y = " + y.toString());

            //Создаем отображение и заполняем данными
            HashMap<String, String> map = new HashMap<>();
            map.put("name", name.toString());
            map.put("x", x.toString());
            map.put("y", y.toString());

            return map;
        }
        else {
            //Закрываем соединение
            try {
                mWorkbook.close();
                mInputStream.close();
            } catch (Exception e) {
                Log.i("Parsing", String.valueOf(e));
            }
            return null;
        }
    }
}