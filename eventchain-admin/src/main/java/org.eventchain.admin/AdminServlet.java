/**
 * Copyright 2016 Eventchain team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.eventchain.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.InvalidSyntaxError;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;

@WebServlet(
        name = "EventchainAdminSerlvet",
        urlPatterns = "/"
)
public class AdminServlet extends HttpServlet {

    private static class QueryContext {

        private final Bundle bundle;

        public QueryContext() {
            bundle = FrameworkUtil.getBundle(this.getClass());
        }

        public Version getVersion() {
            return bundle.getVersion();
        }

        public List<String> getComponents() {
            List<Bundle> bundles = Arrays.asList(bundle.getBundleContext().getBundles());
            return bundles.stream().
                    filter(bundle -> bundle.getSymbolicName().startsWith("eventchain-")).
                    map(bundle -> bundle.getSymbolicName() + " " + bundle.getVersion()).
                    collect(Collectors.toList());
        }
    }

    public static GraphQLObjectType queryType = newObject().name("QueryType").
            field(newFieldDefinition().
                    name("version").
                    type(new GraphQLNonNull(GraphQLString)).
                    dataFetcher(environment -> {
                        QueryContext context = (QueryContext) environment.getContext();
                        return context.getVersion().toString();
                    }).
                    build()).
            field(newFieldDefinition().
                    name("components").
                    type(new GraphQLNonNull(new GraphQLList(GraphQLString))).
                    dataFetcher(environment -> {
                        QueryContext context = (QueryContext) environment.getContext();
                        return context.getComponents();
                    }).build()).
            build();
    public static GraphQLSchema schema = newSchema().query(queryType).build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("q");
        if (query == null) {
            query = "query { version components }";
        }
        ExecutionResult result = new GraphQL(schema).execute(query, new QueryContext());
        resp.setContentType("application/json");
        if (result.getErrors().isEmpty()) {
            resp.getWriter().write(new ObjectMapper().writeValueAsString(result.getData()));
        } else {
            resp.setStatus(500);
            List<GraphQLError> errors = result.getErrors().stream().
                    filter(error -> error instanceof InvalidSyntaxError || error instanceof ValidationError).
                    collect(Collectors.toList());
            resp.getWriter().write(new ObjectMapper().writeValueAsString(errors));
        }
    }
}
