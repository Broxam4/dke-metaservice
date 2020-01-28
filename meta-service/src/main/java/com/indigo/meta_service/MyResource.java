package com.indigo.meta_service;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

@Path("resource/")
public class MyResource {

	private static String wuPrefix = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "PREFIX ont: <http://www.semanticweb.org/ontologies/2019/wu#> "
			+ "PREFIX rdf: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ";

	private static String jkuPrefix = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "PREFIX ont: <http://www.semanticweb.org/andre/ontologies/2019/9/untitled-ontology-14#> "
			+ "PREFIX rdf: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ";

	private static RDFConnection getWuConnection() {
//		return RDFConnectionFactory.connectFuseki("http://140.78.235.245:3030/wu");
		return RDFConnectionFactory.connectFuseki("http://localhost:3030/wu");
	}

	private static RDFConnection getJkuConnection() {
//		return RDFConnectionFactory.connectFuseki("http://140.78.235.245:3030/wu");
		return RDFConnectionFactory.connectFuseki("http://localhost:3030/jku");
	}

	@GET
	@Path("uni/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getUni() {
		System.out.println(String.format("GetUni called!"));
		List<String> unis = new ArrayList<String>();
		unis.add("WU");
		unis.add("JKU");
		return unis;
	}

	private List<Course> wuGetCourse(String coursetype, String name, String courseId, Float ects, String lector) {
		RDFConnection conn = getWuConnection();
		StringBuilder query = new StringBuilder(wuPrefix);
		query.append("SELECT ?name ?coursetype ?ects WHERE {");
		if (coursetype != null)
			query.append(String.format("?subject r:type ont:%s. ", coursetype));
		query.append("?coursetype rdf:subClassOf ont:Lehrveranstaltung. ");
		query.append("?subject r:type ?coursetype. ");
		query.append("?subject rdf:label ?name. ");
		if (name != null)
			query.append(String.format("FILTER regex(?name, %c%s%c, \"i\"). ", '"', name, '"'));
		if (ects != null)
			query.append(String.format("?subject ont:hatECTS %c%.1f%c^^xsd:float. ", '"', ects, '"').replace(',', '.'));
		query.append("OPTIONAL {?subject ont:hatECTS ?ects.} ");
		query.append("}");
		QueryExecution qexec = QueryExecutionFactory.create(query.toString(), conn.fetchDataset());
		List<Course> courses = convertRsCourse(qexec.execSelect());
		qexec.close();
		conn.close();
		return courses;
	}

	private List<Course> jkuGetCourse(String coursetype, String name, String courseId, Float ects, String lector) {
		RDFConnection conn = getJkuConnection();
		StringBuilder query = new StringBuilder(jkuPrefix);
		query.append("SELECT ?name ?coursetype ?ects ?courseID WHERE {");
		if (coursetype != null)
			query.append(String.format("?subject r:type ont:%s. ", coursetype));
		query.append("?subject r:type ?coursetype. ");
		query.append("?coursetype rdf:subClassOf ont:Course. ");
		query.append("?subject ont:hasName ?name. ");
		if (name != null)
			query.append(String.format("FILTER regex(?name, %c%s%c, \"i\"). ", '"', name, '"'));
		if (ects != null)
			query.append(String.format("?subject ont:hasEcts %c%d%c^^xsd:integer. ", '"', (int) Math.round(ects), '"')
					.replace(',', '.'));
		query.append("OPTIONAL {?subject ont:hasEcts ?ects.}. ");
		query.append("OPTIONAL {?subject ont:hasID ?courseID.} ");
		query.append("}");
		QueryExecution qexec = QueryExecutionFactory.create(query.toString(), conn.fetchDataset());
		List<Course> courses = convertRsCourse(qexec.execSelect());
		qexec.close();
		conn.close();
		return courses;
	}

	private List<Course> convertRsCourse(ResultSet rs) {
		List<Course> courses = new ArrayList<>();
		while (rs.hasNext()) {
			QuerySolution soln = rs.nextSolution();
			String name = null;
			String coursetype = "-";
			String ects = "-1";
			if (soln.get("?name") instanceof ResourceImpl)
				name = soln.getResource("?name").getLocalName();
			else if (soln.get("?name") instanceof LiteralImpl)
				name = soln.getLiteral("?name").toString();
			if (soln.get("?coursetype") instanceof ResourceImpl)
				coursetype = soln.getResource("?coursetype").getLocalName();
			else if (soln.get("?coursetype") instanceof LiteralImpl)
				coursetype = soln.getLiteral("?coursetype").toString();
			if (soln.get("?ects") instanceof ResourceImpl)
				ects = soln.getResource("?ects").getLocalName();
			else if (soln.get("?ects") instanceof LiteralImpl)
				ects = soln.getLiteral("?ects").toString();
			if (name != null) {
				courses.add(new Course(name, "-", "-", coursetype,
						Float.parseFloat(ects.replace("\"", "").split("\\^")[0]), false));
			}
		}
		return courses;
	}

	private String resolveCourseType(String coursetype, String uni) {
		if (coursetype == null)
			return null;
		switch (uni) {
		case "wu":
			switch (coursetype) {
			case "ue":
				return "Übung";
			case "vo":
				return "Vorlesung";
			case "ks":
				return "Seminar";
			default:
				return coursetype;
			}
		case "jku":
			switch (coursetype) {
			case "ue":
				return "Uebung";
			case "vo":
				return "Vorlesung";
			case "ks":
				return "Kurs";
			case "pr":
				return "Praktikum";
			default:
				return coursetype;
			}
		default:
			return null;
		}
	}

	@GET
	@Path("getCourses/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Course> getCourses(@QueryParam("coursetype") String coursetype, @QueryParam("name") String name,
			@QueryParam("id") String courseId, @QueryParam("ects") Float ects, @QueryParam("lector") String lector,
			@QueryParam("uni") String uni) {
		if (uni == null)
			uni = "both";
		System.out.println(
				String.format("-%s- GetCoursescalled: %s; %s; %s; %s; %s", uni, coursetype, name, courseId, ects, lector));
		List<Course> courses = new ArrayList<>();
		switch (uni) {
		case "wu":
			courses.addAll(wuGetCourse(resolveCourseType(coursetype, "wu"), name, courseId, ects, lector));
			break;
		case "jku":
			courses.addAll(jkuGetCourse(resolveCourseType(coursetype, "wu"), name, courseId, ects, lector));
			break;
		case "both":
			courses.addAll(wuGetCourse(resolveCourseType(coursetype, "wu"), name, courseId, ects, lector));
			courses.addAll(jkuGetCourse(resolveCourseType(coursetype, "jku"), name, courseId, ects, lector));
			break;
		default:
			break;
		}
		return courses;
	}

	@GET
	@Path("{uni}/studentCourses/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Course> getStudentCourse(@PathParam("uni") String uni, @QueryParam("matrikelNr") String matrikelNr,
			@QueryParam("firstname") String firstname, @QueryParam("lastname") String lastname) {
		System.out
				.println(String.format("-%s- studentCourses called: %s %s; %s", uni, firstname, lastname, matrikelNr));
		List<Course> courses = new ArrayList<>();
		RDFConnection conn;
		StringBuilder query;
		QueryExecution qexec;
		switch (uni.toLowerCase()) {
		case "wu":
			conn = getWuConnection();
			query = new StringBuilder(wuPrefix);
			query.append("SELECT ?name ?coursetype ?ects WHERE {");
			query.append(String.format("ont:%s_%s ont:istAngemeldetFür ?subject. ", firstname, lastname));
			query.append("?subject rdf:label ?name. ");
			query.append("?subject r:type ?coursetype. ");
			query.append("?coursetype rdf:subClassOf ont:Lehrveranstaltung. ");
			query.append("OPTIONAL {?subject ont:hatECTS ?ects.} ");
			query.append("}");
			qexec = QueryExecutionFactory.create(query.toString(), conn.fetchDataset());
			courses.addAll(convertRsCourse(qexec.execSelect()));
			qexec.close();
			conn.close();
			break;
		case "jku":
			conn = getJkuConnection();
			query = new StringBuilder(jkuPrefix);
			query.append("SELECT ?name ?coursetype ?ects WHERE {");
			query.append(String.format("ont:%s ont:attends ?subject. ", matrikelNr));
			query.append("?subject ont:hasName ?name. ");
			query.append("?subject r:type ?coursetype. ");
			query.append("?coursetype rdf:subClassOf ont:Course. ");
			query.append("OPTIONAL {?subject ont:hasEcts ?ects.} ");
			query.append("}");
			qexec = QueryExecutionFactory.create(query.toString(), conn.fetchDataset());
			courses.addAll(convertRsCourse(qexec.execSelect()));
			qexec.close();
			conn.close();
			break;
		}
		return courses;
	}

	@POST
	@Path("{uni}/insertStudent/")
	public Response insertStudent(Student student, @PathParam("uni") String uni) {
		System.out.println(String.format("-%s- insertStudent called: %s %s; %s", uni, student.getFIRST_NAME(),
				student.getLAST_NAME(), student.getMATR_NR()));
		RDFConnection conn;
		StringBuilder query;
		switch (uni.toLowerCase()) {
		case "wu":
			conn = getWuConnection();
			query = new StringBuilder(wuPrefix);
			query.append("INSERT DATA { ");
			query.append(
					String.format("ont:%s_%s r:type ont:Person. ", student.getFIRST_NAME(), student.getLAST_NAME()));
			query.append(String.format("ont:%s_%s ont:hatMatrikelnummer '%s'^^xsd:integer. ", student.getFIRST_NAME(),
					student.getLAST_NAME(), student.getMATR_NR()));
			query.append(String.format("ont:%s_%s rdf:label '%s %s'.}", student.getFIRST_NAME(), student.getLAST_NAME(),
					student.getFIRST_NAME(), student.getLAST_NAME()));
			conn.update(query.toString());
			conn.close();
			break;
		case "jku":
			conn = getJkuConnection();
			query = new StringBuilder(jkuPrefix);
			query.append("INSERT DATA { ");
			query.append("ont:" + student.getMATR_NR() + " r:type ont:Person; ");
			query.append("ont:isEnrolledIn ont:JKU; ");
			query.append("ont:hasFirstName '" + student.getFIRST_NAME() + "'; ");
			query.append("ont:hasLastName '" + student.getLAST_NAME() + "'; ");
			query.append("ont:hasID '" + student.getMATR_NR() + "'^^xsd:integer.} ");
			conn.update(query.toString());
			conn.close();
			break;
		default:
			return Response.status(Response.Status.BAD_REQUEST).entity("Uni does not exsist").build();
		}
		return Response.status(201).entity("Student created!").build();
	}

	@POST
	@Path("{uni}/courseRegistration/")
	public Response courseRegistration(StudentCourseRelation combo, @PathParam("uni") String uni) {
		System.out.println(String.format("-%s- CourseRegistration called: %s %s; %s", uni,
				combo.getStudent().getFIRST_NAME(), combo.getStudent().getLAST_NAME(), combo.getCourse().getName()));
		RDFConnection conn;
		StringBuilder query;
		switch (uni.toLowerCase()) {
		case "wu":
			conn = getWuConnection();
			query = new StringBuilder(wuPrefix);
			query.append("INSERT { ");
			query.append(String.format("ont:%s_%s ont:istAngemeldetFür ?coursename. ",
					combo.getStudent().getFIRST_NAME(), combo.getStudent().getLAST_NAME()));
			query.append("} WHERE {");
			query.append(String.format("?coursename rdf:label %c%s%c. ", '"', combo.getCourse().getName(), '"'));
			query.append("}");
			conn.update(query.toString());
			conn.close();
			break;
		case "jku":
			conn = getJkuConnection();
			query = new StringBuilder(jkuPrefix);
			query.append("INSERT DATA { ");
			query.append(String.format("ont:%s ont:attends ont:%s ", combo.getStudent().getMATR_NR(),
					combo.getCourse().getName().replace(" ", "_")));
			query.append("}");
			conn.update(query.toString());
			conn.close();
			break;
		default:
			return Response.status(Response.Status.BAD_REQUEST).entity("Uni does not exsist").build();
		}
		return Response.status(201).entity("Registration completed!").build();
	}

	@DELETE
	@Path("{uni}/courseRegistration/")
	public Response courseUnsubscribe(StudentCourseRelation combo, @PathParam("uni") String uni) {
		System.out.println(String.format("-%s- CourseUnsubscribe called: %s %s; %s", uni,
				combo.getStudent().getFIRST_NAME(), combo.getStudent().getLAST_NAME(), combo.getCourse().getName()));
		RDFConnection conn;
		StringBuilder query;
		switch (uni.toLowerCase()) {
		case "wu":
			conn = getWuConnection();
			query = new StringBuilder(wuPrefix);
			query.append("DELETE { ");
			query.append(String.format("ont:%s_%s ont:istAngemeldetFür ?coursename ",
					combo.getStudent().getFIRST_NAME(), combo.getStudent().getLAST_NAME()));
			query.append("} WHERE {");
			query.append(String.format("?coursename rdf:label %c%s%c", '"', combo.getCourse().getName(), '"'));
			query.append("}");
			conn.update(query.toString());
			conn.close();
			break;
		case "jku":
			conn = getJkuConnection();
			query = new StringBuilder(jkuPrefix);
			query.append("DELETE DATA { ");
			query.append(String.format("ont:%s ont:attends ont:%s ", combo.getStudent().getMATR_NR(),
					combo.getCourse().getName().replace(" ", "_")));
			query.append("}");
			conn.update(query.toString());
			conn.close();
			break;
		default:
			return Response.status(Response.Status.BAD_REQUEST).entity("Uni does not exsist").build();
		}
		return Response.status(200).entity("Deletion completed!").build();
	}

}