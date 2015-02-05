package ar.rulosoft.mimanganu.test;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Test;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public abstract class TestBase extends TestCase {

	ServerBase server;
	Manga manga;
	Capitulo capitulo;

	@Test
	public void testGetMangas() {
		try {
			if (server.tieneListado()) {
				ArrayList<Manga> mangas = server.getMangas();
				assertTrue(mangas != null && mangas.size() > 5);
			} else {
				assertTrue(true);
			}
		} catch (Exception e) {
			fail("exception" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testCargarCapitulos() {
		try {
			server.cargarCapitulos(manga);
			assertTrue(manga.getCapitulos().size() > 0);
		} catch (Exception e) {
			fail("exception" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testIniciarCapitulo() {
		try {
			server.iniciarCapitulo(capitulo);
			assertTrue(capitulo.getPaginas() > 0);
		} catch (Exception e) {
			fail("exception" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testGetImagen() {
		try {
			String imagen = server.getImagen(capitulo, 2);
			assertTrue(imagen.matches("http:.+"));
		} catch (Exception e) {
			fail("exception" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testGetMangasFiltered() {
		try {
			if (server.tieneNavegacionVisual()) {
				ArrayList<Manga> mangas = server.getMangasFiltered(0, 0, 1);
				assertTrue(mangas.size() > 5);
			} else {
				assertTrue(true);
			}
		} catch (Exception e) {
			fail("exception" + e.getMessage());
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {

	}

}
