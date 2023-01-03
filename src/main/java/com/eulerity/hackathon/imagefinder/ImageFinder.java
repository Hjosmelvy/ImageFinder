package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebServlet(name = "ImageFinder", urlPatterns = { "/main" })
public class ImageFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	// This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
	};

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Cookie testCookie = new Cookie("test", "mycookie");
		testCookie.setHttpOnly(true);
		testCookie.setPath("/");
		testCookie.setMaxAge(3600);
		testCookie.setSecure(true);
		testCookie.setComment("__SAME_SITE_NONE__");
		// This can be __SAME_SITE_NONE__, or __SAME_SITE_STRICT__, or __SAME_SITE_LAX__

		resp.addCookie(testCookie);
		String path = req.getServletPath();
		String url = req.getParameter("url");
		System.out.println("Got request of:" + path + " with query param:" + url);

		if (url == null)
			return;

		List<DisplayImage> displayImages;
		List<List<String>> images = new ArrayList<>();
		displayImages = getImagesIncludingSubpages(url);

		displayImages.forEach((displayImage) -> {

			String[] x = { displayImage.getUrl(), String.valueOf(displayImage.isPerson()) };
			images.add(Arrays.asList(x));

		});

		resp.getWriter().print(GSON.toJson(images));

	}

	// gets all src attributes from all <img> tags
	protected final List<String> getImages(String url) throws IOException {

		Document doc = Jsoup.connect(url).get();
		List<String> images = new ArrayList<String>();
		doc.select("img[src]").forEach(x -> images.add(x.attr("src")));

		List<String> result = removeDuplicates(images);
		return result;
	}

	// gets all href from all <a> tags
	protected final List<String> getSubLinks(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		List<String> links = new ArrayList<String>();

		doc.select("a[href]").forEach((link) -> {

			if (isValid(link.attr("href"))) {
				links.add(link.attr("href"));

			}

		});

		List<String> result = removeDuplicates(links);

		return result;

	}

	// finds all subpages, collects all images from main url and subpages, return
	// the images in all links
	protected final List<DisplayImage> getImagesIncludingSubpages(String url) throws IOException {
		List<String> images = new ArrayList<>();
		List<String> links = new ArrayList<>();
		List<DisplayImage> result = new ArrayList<>();

		links.add(url);
		links.addAll(getSubLinks(url));

		// temp is a temporary variable used to convert the List<List<String>>
		// containing all images from all links to List<String>

		List<String> temp = links.parallelStream().map((link) -> {
			try {
				return getImages(link.toString());
			} catch (IOException e) {
				return null;
			}
		}).collect(Collectors.toList()).stream()
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		synchronized (temp) {
			images = removeDuplicates(temp);
		}

		images.parallelStream().forEach((image) -> {
			try {
				System.out.println(image);
				result.add(new DisplayImage(image));
			} catch (IOException e) {
				e.printStackTrace();

			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		});

		return result;

	}

	protected final List<String> removeDuplicates(List<String> list) {
		Set<String> set = new LinkedHashSet<>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
		return list;
	}

	// checks if a url is valid
	protected boolean isValid(String url) {
		/* Try creating a valid URL */
		try {
			new URL(url).toURI();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
