package ar.rulosoft.mimanganu.componentes;

public class Capitulo {
	
	public static final int NUEVO = - 1;
	public static final int SIN_LEER = 0;
	public static final int LEIDO = 1;
	
	int id, paginas, mangaID;
	int pagLeidas, estadoLectura;
	String titulo, path;
	boolean finalizado, descargado;

	public int getMangaID() {
		return mangaID;
	}

	public void setMangaID(int mangaID) {
		this.mangaID = mangaID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPaginas() {
		return paginas;
	}

	public void setPaginas(int pagina) {
		this.paginas = pagina;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isFinalizado() {
		return finalizado;
	}

	public void setFinalizado(boolean finalizado) {
		this.finalizado = finalizado;
	}

	@Override
	public String toString() {
		return titulo;
	}

	public Capitulo(String titulo, String path) {
		super();
		this.titulo = titulo;
		this.path = path;
	}

	public int getPagLeidas() {
		return pagLeidas;
	}

	public void setPagLeidas(int pagLeidas) {
		this.pagLeidas = pagLeidas;
	}

	public int getEstadoLectura() {
		return estadoLectura;
	}

	public void setEstadoLectura(int estadoLectura) {
		this.estadoLectura = estadoLectura;
	}

	public boolean isDescargado() {
		return descargado;
	}

	public void setDescargado(boolean descargado) {
		this.descargado = descargado;
	}

}
