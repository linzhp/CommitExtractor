package edu.ucsc.cs.sil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


public class File implements IFile {
	private String contentString;
	
	public String getContentString() {
		return contentString;
	}

	public void setContentString(String contentString) {
		this.contentString = contentString;
	}

	@Override
	public String toString(){
		return contentString;
	}
	@Override
	public IContentDescription getContentDescription() throws CoreException{
		return null;
	}

	@Override
	public void accept(IResourceVisitor arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(IResourceProxyVisitor arg0, int arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(IResourceVisitor arg0, int arg1, boolean arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(IResourceVisitor arg0, int arg1, int arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearHistory(IProgressMonitor arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copy(IPath arg0, boolean arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copy(IPath arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copy(IProjectDescription arg0, boolean arg1,
			IProgressMonitor arg2) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copy(IProjectDescription arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public IMarker createMarker(String arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResourceProxy createProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(boolean arg0, IProgressMonitor arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(int arg0, IProgressMonitor arg1) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMarkers(String arg0, boolean arg1, int arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IMarker findMarker(long arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMarker[] findMarkers(String arg0, boolean arg1, int arg2)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int findMaxProblemSeverity(String arg0, boolean arg1, int arg2)
			throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getFileExtension() {
		return ".java";
	}

	@Override
	public long getLocalTimeStamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IPath getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getLocationURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMarker getMarker(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getModificationStamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IContainer getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPathVariableManager getPathVariableManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getPersistentProperties() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPersistentProperty(QualifiedName arg0)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getProjectRelativePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getRawLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getRawLocationURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceAttributes getResourceAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getSessionProperties() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSessionProperty(QualifiedName arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IWorkspace getWorkspace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAccessible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDerived(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLinked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLinked(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLocal(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPhantom() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSynchronized(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTeamPrivateMember() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTeamPrivateMember(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(IPath arg0, boolean arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(IPath arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(IProjectDescription arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(IProjectDescription arg0, boolean arg1, boolean arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshLocal(int arg0, IProgressMonitor arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void revertModificationStamp(long arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDerived(boolean arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDerived(boolean arg0, IProgressMonitor arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHidden(boolean arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocal(boolean arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public long setLocalTimeStamp(long arg0) throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPersistentProperty(QualifiedName arg0, String arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReadOnly(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setResourceAttributes(ResourceAttributes arg0)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSessionProperty(QualifiedName arg0, Object arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTeamPrivateMember(boolean arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void touch(IProgressMonitor arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(ISchedulingRule arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConflicting(ISchedulingRule arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void appendContents(InputStream arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void appendContents(InputStream arg0, boolean arg1, boolean arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void create(InputStream is, boolean b, IProgressMonitor pm)
			throws CoreException {
	}

	@Override
	public void create(InputStream arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createLink(IPath arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createLink(URI arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(boolean arg0, boolean arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCharset() throws CoreException {
		return "UTF-8";
	}

	@Override
	public String getCharset(boolean arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharsetFor(Reader arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getContents() throws CoreException {
		
		return new ByteArrayInputStream(contentString.getBytes());
	}

	@Override
	public InputStream getContents(boolean arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEncoding() throws CoreException {
		return IFile.ENCODING_UTF_8;
	}

	@Override
	public IPath getFullPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFileState[] getHistory(IProgressMonitor arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(IPath arg0, boolean arg1, boolean arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharset(String arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharset(String arg0, IProgressMonitor arg1)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContents(InputStream arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContents(IFileState arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContents(InputStream arg0, boolean arg1, boolean arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContents(IFileState arg0, boolean arg1, boolean arg2,
			IProgressMonitor arg3) throws CoreException {
		// TODO Auto-generated method stub

	}

}
