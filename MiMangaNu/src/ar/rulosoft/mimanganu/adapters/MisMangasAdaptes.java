package ar.rulosoft.mimanganu.adapters;

import java.util.ArrayList;
import java.util.List;

import com.fedorvlasov.lazylist.ImageLoader;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import ar.rulosoft.mimanganu.componentes.ControlTapaSerie;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.R;

public class MisMangasAdaptes extends ArrayAdapter<Manga> {

	private static int resource = R.layout.listitem_mis_mangas;
	private ImageLoader imageLoader;
	Activity c;
	ViewHolder holder;

	public MisMangasAdaptes(Activity context, List<Manga> objects) {
		super(context, resource, objects);
		c = context;
		imageLoader = new ImageLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = convertView;
		if (item == null) {
			LayoutInflater inflater = c.getLayoutInflater();
			item = inflater.inflate(resource, null);
			holder = new ViewHolder();
			holder.serie = (ControlTapaSerie) item.findViewById(R.id.tapa);
			holder.notif = (ImageView) item.findViewById(R.id.notif);
			item.setTag(holder);
		} else {
			holder = (ViewHolder) item.getTag();
		}

		Manga s = (Manga) getItem(position);
		holder.serie.setText(s.getTitulo());
		imageLoader.DisplayImage(s.getImages(), holder.serie);
		if(s.getNuevos()>0){
			holder.notif.setVisibility(ImageView.VISIBLE);
		}else{
			holder.notif.setVisibility(ImageView.INVISIBLE);
		}
		
		return (item);
	}
	
	public void addAll(ArrayList<Manga> mangasNuevos){
		for (Manga manga : mangasNuevos) {
			add(manga);
		}	
	}

	static class ViewHolder {
		public ImageView notif;
		public ControlTapaSerie serie;
	}

}
