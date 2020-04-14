package al.ahgitdevelopment.municion.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import al.ahgitdevelopment.municion.R;

import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_APODO;
import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CUPO;
import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_GASTADO;

/**
 * Created by Alberto on 15/04/2016.
 */
public class GuiaCursorAdapter extends CursorAdapter {

    public GuiaCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imagen = view.findViewById(R.id.imageArma);
        TextView apodo = view.findViewById(R.id.item_apodo_guia);
        TextView cupo = view.findViewById(R.id.item_cupo_guia);
        TextView gastado = view.findViewById(R.id.item_gastados_guia);

//        if (!cursor.getBlob(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_IMAGEN)))
//            imagen.
//        )

        apodo.setText(cursor.getString(cursor.getColumnIndex(KEY_GUIA_APODO)));
        int numCupo = cursor.getInt(cursor.getColumnIndex(KEY_GUIA_CUPO));
        int numGastado = cursor.getInt(cursor.getColumnIndex(KEY_GUIA_GASTADO));
        cupo.setText(new StringBuilder().append(numCupo));
        gastado.setText(new StringBuilder().append((1.0 * numGastado / numCupo) * 100 + "%"));

//        Bitmap bitmap = BitmapFactory.decodeByteArray((byte[]) cursor.getBlob(
//                cursor.getColumnIndex( KEY_GUIA_IMAGEN)));

//        imagen.setImageBitmap(bitmap);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.guia_item, null, false);
        return view;
    }


//    private byte[] getLogoImage(String url) {
//        try {
//            InputStream is = ucon.getInputStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//
//            ByteArrayBuffer baf = new ByteArrayBuffer(500);
//            int current = 0;
//            while ((current = bis.read()) != -1) {
//                baf.append((byte) current);
//            }
//
//            return baf.toByteArray();
//        } catch (Exception e) {
//            Log.d("ImageManager", "Error: " + e.toString());
//        }
//        return null;
//    }
}
