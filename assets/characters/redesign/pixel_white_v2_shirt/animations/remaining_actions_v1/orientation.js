(function(root){
 'use strict';
 const subtract=(a,b)=>({x:a.x-b.x,y:a.y-b.y});
 const radians=d=>d*Math.PI/180;
 function matrixAt(localPoint,worldPoint,rotation,facing){
  const c=Math.cos(rotation),s=Math.sin(rotation);
  const a=c*facing,b=s*facing,cc=-s,d=c;
  return {a,b,c:cc,d,e:worldPoint.x-a*localPoint.x-cc*localPoint.y,f:worldPoint.y-b*localPoint.x-d*localPoint.y,rotation,facing};
 }
 function transform(m,p){return{x:m.a*p.x+m.c*p.y+m.e,y:m.b*p.x+m.d*p.y+m.f}}
 function pendulum(frame,anchor,angleDegrees,ropeLength,direction){
  const theta=radians(angleDegrees),radial={x:Math.sin(theta),y:Math.cos(theta)};
  const facing=direction<0?-1:1;
  const axis=subtract(frame.hip,frame.hand);axis.x*=facing;
  const rotation=Math.atan2(radial.y,radial.x)-Math.atan2(axis.y,axis.x);
  const hand={x:anchor.x+radial.x*ropeLength,y:anchor.y+radial.y*ropeLength};
  const m=matrixAt(frame.hand,hand,rotation,facing);
  return {matrix:m,hand,hip:transform(m,frame.hip),radial,tangent:{x:Math.cos(theta)*facing,y:-Math.sin(theta)*facing}};
 }
 function propulsion(frame,worldHip,target){
  const aim=subtract(target,worldHip),facing=aim.x<0?-1:1;
  const axis=subtract(frame.hand,frame.hip);axis.x*=facing;
  const rotation=Math.atan2(aim.y,aim.x)-Math.atan2(axis.y,axis.x);
  const matrix=matrixAt(frame.hip,worldHip,rotation,facing);
  return {matrix,hand:transform(matrix,frame.hand),hip:worldHip};
 }
 function orbitingBody(frame,anchor,angleDegrees,hipRadius,direction){
  const theta=radians(angleDegrees),facing=direction<0?-1:1;
  const radial={x:Math.sin(theta),y:Math.cos(theta)};
  const hip={x:anchor.x+radial.x*hipRadius,y:anchor.y+radial.y*hipRadius};
  const matrix=matrixAt(frame.hip,hip,-theta,facing);
  return {matrix,hand:transform(matrix,frame.hand),hip,radial,tangent:{x:Math.cos(theta)*facing,y:-Math.sin(theta)*facing}};
 }
 const api={pendulum,propulsion,orbitingBody,transform};
 if(typeof module!=='undefined'&&module.exports)module.exports=api;
 if(root)root.SpriteOrientation=api;
})(typeof window!=='undefined'?window:globalThis);
