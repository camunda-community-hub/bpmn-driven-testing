import chai from "chai";

import { getMarkers, pathEquals } from "../main/bpmndt/functions";

const expect = chai.expect;

describe("functions", () => {
  describe("getMarkers", () => {
    it("should get different markers for start, path and end", () => {
      const markers = getMarkers({
        start: "a",
        path: ["a", "b", "c"],
        end: "c"
      });

      expect(markers).to.have.lengthOf(3);
      expect(markers[0].id).to.equal("a");
      expect(markers[1].id).to.equal("b");
      expect(markers[2].id).to.equal("c");
      expect(markers[0].style !== markers[1].style).to.be.true;
      expect(markers[0].style !== markers[2].style).to.be.true;
      expect(markers[1].style !== markers[2].style).to.be.true;
    });

    it("should only mark start and end", () => {
      const markers = getMarkers({
        start: "startEvent",
        path: [],
        end: "endEvent"
      });

      expect(markers).to.have.lengthOf(2);
      expect(markers[0].id).to.equal("startEvent");
      expect(markers[1].id).to.equal("endEvent");
      expect(markers[0].style !== markers[1].style).to.be.true;
    });
  });

  describe("pathEquals", () => {
    let path;

    beforeEach(() => {
      path = ["a", "b", "c"];
    });
  
    it("should be equal to path", () => {
      expect(pathEquals(path, ["a", "b", "c"])).to.be.true;
    });
  
    it("should not be equal to path", () => {
      expect(pathEquals(path, ["a", "b", "c", "d"])).to.be.false;
      expect(pathEquals(path, ["a", "b", "x"])).to.be.false;
      expect(pathEquals(path, ["a", "b"])).to.be.false;
      expect(pathEquals(path, ["x"])).to.be.false;
      expect(pathEquals(path, [])).to.be.false;
    });
  });
});
