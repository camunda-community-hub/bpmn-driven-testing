import chai from "chai";

import TestCase from "../main/bpmn-driven-testing/TestCase";

const expect = chai.expect;

describe("TestCase", () => {
  let testCase;

  beforeEach(() => {
    testCase = new TestCase({path: ["a", "b", "c"]});
  });

  it("should be equal to path", () => {
    expect(testCase.equals(["a", "b", "c"])).to.be.true;
  });

  it("should not be equal to path", () => {
    expect(testCase.equals(["a", "b", "c", "d"])).to.be.false;
    expect(testCase.equals(["a", "b", "x"])).to.be.false;
    expect(testCase.equals(["a", "b"])).to.be.false;
    expect(testCase.equals(["x"])).to.be.false;
    expect(testCase.equals([])).to.be.false;
  });

  it("should update flow node ID when it is in the path", () => {
    testCase.update("a", "x");
    testCase.update("b", "y");
    testCase.update("c", "z");

    expect(testCase.path[0]).to.equal("x");
    expect(testCase.path[1]).to.equal("y");
    expect(testCase.path[2]).to.equal("z");
  });

  it("should not update flow node ID when it is not in the path", () => {
    testCase.update("x", "y");

    expect(testCase.path[0]).to.equal("a");
    expect(testCase.path[1]).to.equal("b");
    expect(testCase.path[2]).to.equal("c");
  });
});
