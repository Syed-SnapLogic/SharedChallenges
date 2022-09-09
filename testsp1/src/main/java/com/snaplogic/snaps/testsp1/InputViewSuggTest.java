package com.snaplogic.snaps.testsp1;

import com.snaplogic.api.InputSchemaProvider;
import com.snaplogic.common.SnapType;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.*;
import com.snaplogic.snap.schema.api.ObjectSchema;
import com.snaplogic.snap.schema.api.Schema;
import com.snaplogic.snap.schema.api.SchemaBuilder;
import com.snaplogic.snap.schema.api.SchemaProvider;

@General(title="Input V S Test", purpose = "ivst", docLink = "http://google.com")
@Inputs(min=1,max=1,accepts = ViewType.DOCUMENT)
@Outputs(min=0, max=1, offers = ViewType.DOCUMENT)
@Version(snap = 1)
@Category(snap = SnapCategory.WRITE)
public class InputViewSuggTest extends SimpleSnap implements InputSchemaProvider {
    @Override
    protected void process(Document document, String s) {
        outputViews.write(documentUtility.newDocument("hi syed"));
    }

    @Override
    public void defineInputSchema(SchemaProvider schemaProvider) {
        String ivn = schemaProvider.getRegisteredInputViewNames().iterator().next();
        SchemaBuilder b = schemaProvider.getSchemaBuilder(ivn);
        Schema name = schemaProvider.createSchema(SnapType.STRING, "name");
        Schema id = schemaProvider.createSchema(SnapType.INTEGER, "id");
        // address
        Schema country = schemaProvider.createSchema(SnapType.STRING, "country");
        Schema city = schemaProvider.createSchema(SnapType.STRING, "city");
        Schema state = schemaProvider.createSchema(SnapType.STRING, "state");
        ObjectSchema address = schemaProvider.createSchema(SnapType.COMPOSITE, "address");
        address.addChild(country); address.addChild(city); address.addChild(state);
        // hobbies
        ObjectSchema hobbies = schemaProvider.createSchema(SnapType.TABLE, "hobbies");
        Schema hobby = schemaProvider.createSchema(SnapType.STRING, null);
        hobbies.addChild(hobby);
        ObjectSchema exp = schemaProvider.createSchema(SnapType.TABLE, "experience");
        ObjectSchema expObject = schemaProvider.createSchema(SnapType.COMPOSITE, null);
        expObject.addChild(schemaProvider.createSchema(SnapType.STRING, "companyName"));
        expObject.addChild(schemaProvider.createSchema(SnapType.INTEGER, "id"));
        expObject.addChild(schemaProvider.createSchema(SnapType.NUMBER, "ACTC"));
        exp.addChild(expObject);
        b.withChildSchema(name)
                .withChildSchema(id)
                .withChildSchema(address)
                .withChildSchema(hobbies)
                .withChildSchema(exp)
                .build();
    }
}
