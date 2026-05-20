import {readJSON} from "./util/tutils";
import {AllNodeVisitor} from "../src/io/apitomy/datamodels/models/visitors/AllNodeVisitor";
import {Node} from "../src/io/apitomy/datamodels/models/Node";
import {Library} from "../src/io/apitomy/datamodels/Library";
import {Document} from "../src/io/apitomy/datamodels/models/Document";
import {TraverserDirection} from "../src/io/apitomy/datamodels/TraverserDirection";
import {NodePath} from "../src/io/apitomy/datamodels/paths/NodePath";


class ExtraPropertyDetectionVisitor extends AllNodeVisitor {
    
    public extraProperties: string[] = [];

    public getExtraPropertyCount(): number {
        return this.extraProperties.length;
    }

    visitNode(node: Node): void {
        node.getExtraPropertyNames().forEach(name => {
            this.extraProperties.push(name);
        });
    }

}

class AllNodeFinder extends AllNodeVisitor {
    
    allNodes: Node[] = [];
    
    visitNode(node: Node): void {
        this.allNodes.push(node);
    }

}


interface TestSpec {
    name: string;
    test: string;
    extraProperties?: number;
}


let allTests: TestSpec[] = readJSON("tests/fixtures/io/tests.json");
allTests.forEach(spec => {
    test(spec.name, () => {
        // Read the original/source file JSON
        let testPath: string = "tests/fixtures/io/" + spec.test;
        let json: any = readJSON(testPath);
        expect(json).not.toBeNull();
        
        // Parse/read the document
        let document: Document = Library.readDocument(json);
        
        // Make sure the correct # of extra properties were read
        let extraPropVis: ExtraPropertyDetectionVisitor = new ExtraPropertyDetectionVisitor();
        Library.visitTree(document, extraPropVis, TraverserDirection.down);
        let actualExtraProps: number = extraPropVis.getExtraPropertyCount();
        let expectedExtraProps: number = spec.extraProperties || 0;
        if (actualExtraProps !== expectedExtraProps) {
            throw Error("Detected unexpected extra properties: " + extraPropVis.extraProperties);
        }
        expect(actualExtraProps).toEqual(expectedExtraProps);

        // Serialize/write the document
        let jsObj: any = Library.writeNode(document);

        // Compare what we started with vs. what we now have
        expect(jsObj).toEqual(json);
        
        // Now make sure we can create node paths from **every** node in the doc
        let nodeFinder: AllNodeFinder = new AllNodeFinder();
        Library.visitTree(document, nodeFinder, TraverserDirection.down);
        nodeFinder.allNodes.forEach(node => {
            let nodePath: NodePath = Library.createNodePath(node);
            expect(nodePath).not.toBeNull();
            let path: string = nodePath.toString();
            expect(path).not.toBeNull();
            nodePath = NodePath.parse(path);
            let resolvedNode: Node = Library.resolveNodePath(nodePath, document);
            if (resolvedNode === null) {
                throw Error("Node Path failed to resolve: " + nodePath.toString());
            }
            expect(resolvedNode).not.toBeNull();
            expect(resolvedNode).toBe(node);
        });
    });
});
