/*
 * Copyright (c) 2008-2010, James Leigh and Zepheira LLC Some rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package wwwc.nees.joint.compiler.model;

import java.io.File;
import java.io.IOException;

import wwwc.nees.joint.compiler.annotations.Iri;
import wwwc.nees.joint.compiler.annotations.Prefix;
import wwwc.nees.joint.compiler.model.rdf.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import wwwc.nees.joint.compiler.JavaNameResolver;
import wwwc.nees.joint.compiler.source.JavaMessageBuilder;
import wwwc.nees.joint.compiler.exceptions.ObjectStoreConfigException;

/**
 * Utility class for working with an OWL ontology in a model.
 * 
 * @author James Leigh
 * 
 */
public class RDFOntology extends RDFEntity {

	public RDFOntology(Model model, Resource self) {
		super(model, self);
	}

	public File generatePackageInfo(File dir, String namespace,
			JavaNameResolver resolver) throws IOException,
			ObjectStoreConfigException {
		String pkg = resolver.getPackageName(new URIImpl(namespace));
		File source = createSourceFile(dir, pkg, resolver);
		JavaMessageBuilder builder = new JavaMessageBuilder(source, resolver);
		packageInfo(namespace, builder);
		builder.close();
		return source;
	}

	private void packageInfo(String namespace, JavaMessageBuilder builder)
			throws ObjectStoreConfigException {
		builder.comment(this);
		builder.annotationProperties(this);
		builder.annotateString(Prefix.class.getName(), builder
				.getMemberPrefix(namespace));
		builder.annotateString(Iri.class.getName(), namespace);
		builder.pkg(builder.getPackageName(new URIImpl(namespace)));
	}

	private File createSourceFile(File dir, String pkg,
			JavaNameResolver resolver) {
		String simple = "package-info";
		File folder = dir;
		if (pkg != null) {
			folder = new File(dir, pkg.replace('.', '/'));
		}
		folder.mkdirs();
		File source = new File(folder, simple + ".java");
		return source;
	}
}
